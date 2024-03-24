package com.yourpackage.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Headers

interface ApiService {
    @Headers("Content-Type: application/json")
    @POST("auth/mobile-login")
    fun login(@Body credentials: HashMap<String, String>): Call<HashMap<String, String>>
}