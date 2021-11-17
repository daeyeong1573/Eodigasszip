package org.gsm.software.eodigasszip.view.activity

import android.app.ActivityOptions
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isGone
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.maps.android.clustering.ClusterManager
import org.gsm.software.eodigasszip.BuildConfig
import org.gsm.software.eodigasszip.R
import org.gsm.software.eodigasszip.base.BaseActivity
import org.gsm.software.eodigasszip.databinding.ActivityMapsBinding
import org.gsm.software.eodigasszip.databinding.HeaderBinding
import org.gsm.software.eodigasszip.model.ClusterRenderer
import org.gsm.software.eodigasszip.model.MyItem
import org.gsm.software.eodigasszip.util.Util
import org.gsm.software.eodigasszip.viewmodel.MapViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.ln

class MapsActivity() :
    BaseActivity<ActivityMapsBinding>(R.layout.activity_maps), OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mMap: GoogleMap
    var clusterManager: ClusterManager<MyItem>? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var requestActivity: ActivityResultLauncher<Intent>
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val utill = Util()
    private lateinit var headB: HeaderBinding
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        slideInit()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()

        //구글 초기화
        initGoolgLogin()
        //actionBar 생성
        actionBar()
        //ResultActivity 설정 -> Firebase Intent
        resultActivity()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private fun initViews() = with(binding){
        binding.maps = this@MapsActivity
        binding.navigationView.setNavigationItemSelectedListener(this@MapsActivity)
        val headV = binding.navigationView.getHeaderView(0)
        headB = HeaderBinding.bind(headV)
        headB.activity = this@MapsActivity

    }

    private fun slideInit(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            with(window) {
                requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
                // set an slide transition
                enterTransition = Slide(Gravity.END)
                exitTransition = Slide(Gravity.START)
            }
        }
    }

    
    // menubar 생성
    fun actionBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menubar)
    }

    //메뉴 안의 Navigtion 버튼 함수
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.setting->{

            }

            R.id.carrer->{

            }

            R.id.github_connect->{

            }

            R.id.blog_connect->{

            }

            R.id.profile ->{
                val intent = Intent(this,InputUserActivity::class.java)
                startActivity(intent,ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            }
            R.id.logout -> {
                logout()
            }
        }
        return false
    }

    
    //메뉴 버튼 
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                binding.drawerLayout.openDrawer(GravityCompat.START)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    
    //구글 초기화
    private fun initGoolgLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    //구글 로그인 토큰 보내기
    private fun resultActivity() {
        requestActivity = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Toast.makeText(this, "구글 로그인 실패", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "onActivityResult: $e")
                }
            }
        }
    }


    //구글 로그인
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (utill.useRegex(auth.currentUser?.email.toString())) {
                        Log.d(TAG, "firebaseAuthWithGoogle: 성공")
                        headB.hLogin.visibility = View.GONE
                        headB.hName.visibility = View.VISIBLE
                        headB.hMajor.visibility = View.VISIBLE

                    } else {
                        Toast.makeText(this, "학교 계정을 사용해주세요", Toast.LENGTH_SHORT).show()
                        auth.currentUser?.delete()
                        googleSignInClient.signOut()
                        googleSignInClient.revokeAccess()
                    }

                } else {
                    Log.d(TAG, "firebaseAuthWithGoogle: ${auth.currentUser?.email}")
                    Log.d(TAG, "firebaseAuthWithGoogle: 실패")
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "구글 로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    
    //구글맵 작성
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //초기 지도 위치 세팅
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.1429503, 126.8005143), 13.0f))
        clusterManager = ClusterManager(this, mMap)
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)
        addMaker()
    }

    private fun addMaker() {
        var lat = 35.1429503
        var lng = 126.8005143
        var title = "t"
        var sni = "s"

        for (i in 0..9) {
            val offset = i / 30.0
            lat += offset
            lng += offset
            title = title + "$i"
            sni = sni + "$i"
            val offsetItem =
                MyItem(LatLng(lat, lng), title, sni)
            clusterManager?.addItem(offsetItem)
        }
        clusterManager?.cluster()

    }

    //로그인
    fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        requestActivity.launch(signInIntent)
    }


    //로그아웃 함수
    fun logout() {
        googleSignInClient.signOut().addOnCanceledListener {
            Toast.makeText(this, "로그아웃", Toast.LENGTH_SHORT).show()
        }
        headB.hName.visibility = View.GONE
        headB.hMajor.visibility = View.GONE
        headB.hLogin.visibility = View.VISIBLE
        googleSignInClient.revokeAccess()
    }

    
    //searchView로 이동
    fun goSearch(){
        val intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
    }


    //뒤로가기 2번 클릭시 종료
    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            Toast.makeText(this, "'뒤로' 버튼을 한번더 누르시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
            return
        } else if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }


}