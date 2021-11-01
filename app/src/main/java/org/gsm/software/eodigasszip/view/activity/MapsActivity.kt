package org.gsm.software.eodigasszip.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import org.gsm.software.eodigasszip.R
import org.gsm.software.eodigasszip.databinding.ActivityMapsBinding
import org.gsm.software.eodigasszip.model.ClusterRenderer
import org.gsm.software.eodigasszip.model.MyItem
import org.gsm.software.eodigasszip.viewmodel.MapViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.ln

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    var clusterManager: ClusterManager<MyItem>? = null
    val viewModel : MapViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.maps = this

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //초기 지도 위치 세팅
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.1429503,126.8005143),5.0f))
        clusterManager = ClusterManager(this,mMap)
        mMap.setOnCameraIdleListener(clusterManager)
        mMap.setOnMarkerClickListener(clusterManager)
        addMaker()
    }

    private fun addMaker(){
        var lat = 35.1429503
        var lng = 126.8005143
        var title = "t"
        var sni = "s"

        for (i in 0..9) {
            val offset = i / 30.0
            lat += offset
            lng += offset
            title = title+"$i"
            sni = sni+"$i"
            val offsetItem =
                MyItem(LatLng(lat, lng),title,sni)
            clusterManager?.addItem(offsetItem)
        }
        clusterManager?.cluster()

    }


}