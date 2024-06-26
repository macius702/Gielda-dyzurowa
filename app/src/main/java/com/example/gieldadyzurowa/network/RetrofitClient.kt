package com.example.gieldadyzurowa.network

import com.example.gieldadyzurowa.types.DutySlotStatus
import com.example.gieldadyzurowa.types.DutySlotStatusDeserializer
import com.google.gson.GsonBuilder
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

val loggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .cookieJar(cookieJar)
    .build()


object RetrofitClient {

    //for local node server
    //private const val BASE_URL = "http://10.0.2.2:3000"

    //for local ICP canister
    private const val BASE_URL = "http://10.0.2.2:4943"

    public const val BASE_CANISTER = "bkyz2-fmaaa-aaaaa-qaaaq-cai"



    //private const val BASE_URL = "https://powerful-sea-67789-a7c9da8bf02d.herokuapp.com/" //TODO(mtlk): into an env variable

    val gson =
        GsonBuilder().registerTypeAdapter(DutySlotStatus::class.java, DutySlotStatusDeserializer())
            .create()

    private val retrofit =
        Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient).build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
