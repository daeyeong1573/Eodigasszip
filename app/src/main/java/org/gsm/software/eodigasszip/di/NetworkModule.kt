package org.gsm.software.eodigasszip.di

import com.google.android.gms.common.api.Api
import org.gsm.software.eodigasszip.BuildConfig
import org.gsm.software.eodigasszip.viewmodel.MapViewModel
import org.gsm.software.eodigasszip.viewmodel.SignInViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

var retrofitModule= module {
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    single {
        get<Retrofit>().create(
            Api::class.java
        )
    }
}



var viewModelPart = module {
    viewModel { MapViewModel(get()) }
    viewModel { SignInViewModel(get()) }
}


val myModule = listOf(retrofitModule, viewModelPart)