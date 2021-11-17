package org.gsm.software.eodigasszip.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.gsm.software.eodigasszip.R
import org.gsm.software.eodigasszip.databinding.ActivityMainBinding
import org.koin.android.ext.android.bind

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() = with(binding){
        binding.activity = this@MainActivity
    }

    fun goBack(){
        finish()
    }

}