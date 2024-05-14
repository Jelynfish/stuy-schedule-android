package com.jelynfish.stuyscheduleapp.api

import retrofit2.Call
import retrofit2.http.GET

interface Api {
    @GET
    fun getData(): Call<ApiData>
}