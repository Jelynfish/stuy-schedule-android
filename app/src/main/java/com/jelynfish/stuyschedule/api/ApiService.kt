package com.jelynfish.stuyschedule.api

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("api/weekly-schedule")
    fun getData(): Call<ApiData>
}