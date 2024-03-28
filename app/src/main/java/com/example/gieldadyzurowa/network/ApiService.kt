package com.example.gieldadyzurowa.network

import com.example.gieldadyzurowa.DutySlot
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/auth/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<Void> // Or Call<ResponseType> if your API returns a response

    @GET("/duty/slots")
    suspend fun fetchDutySlots(): Response<List<DutySlot>>
}

data class LoginRequest(val username: String, val password: String)
