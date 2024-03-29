package com.example.gieldadyzurowa.network

import com.example.gieldadyzurowa.DutyVacancy
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/auth/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<Void> // Or Call<ResponseType> if your API returns a response

    @POST("/auth/register")
    fun registerUser(@Body registrationRequest: RegistrationRequest): Call<Void>

    @GET("/duty/slots/json")
    suspend fun fetchDutyVacancies(): Response<List<DutyVacancy>>

    @POST("duty/publish")
    suspend fun publishDutyVacancy(
        @Body dutyVacancy: DutyVacancy
    ): Response<Unit> // Adjust return type based on your API response

    }
    


data class LoginRequest(val username: String, val password: String)
data class RegistrationRequest(
    val username: String,
    val password: String,
    val role: String,
    val specialty: String?,
    val localization: String?
)

