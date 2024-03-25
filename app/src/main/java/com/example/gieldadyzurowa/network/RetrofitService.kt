package com.example.gieldadyzurowa.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {
    //private const val BASE_URL = "https://powerful-sea-67789-a7c9da8bf02d.herokuapp.com/"
    //private const val BASE_URL = "http://localhost:3000/"

    private const val BASE_URL = "http://10.0.2.2:3000/"

    fun <T> createService(serviceClass: Class<T>): T {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(serviceClass)
    }
}