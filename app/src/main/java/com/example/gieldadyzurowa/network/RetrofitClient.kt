package com.example.gieldadyzurowa.network

import com.example.gieldadyzurowa.types.DutySlotStatus
import com.example.gieldadyzurowa.types.DutySlotStatusDeserializer
import com.google.gson.GsonBuilder
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val cookieJar = object : CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookieStore[url.host] = cookies
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: ArrayList()
    }
}

val okHttpClient = OkHttpClient.Builder().cookieJar(cookieJar).build()


object RetrofitClient {
    //private const val BASE_URL = "https://powerful-sea-67789-a7c9da8bf02d.herokuapp.com/" //TODO(mtlk): into an env variable
    private const val BASE_URL = "http://10.0.2.2:3000"

    val gson =
        GsonBuilder().registerTypeAdapter(DutySlotStatus::class.java, DutySlotStatusDeserializer())
            .create()

    private val retrofit =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient).build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
