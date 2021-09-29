package org.gsm.software.eodigasszip.model

import com.google.android.gms.maps.model.LatLng

data class Company(
    //위치
    val lat : LatLng?,
    //선배
    val elder :String?,
    //선배 인수
    val index : Int?,
    //회사 정보
    val companyInform : String
)da