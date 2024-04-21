package com.example.gieldadyzurowa.network

import com.example.gieldadyzurowa.types.AssignDutySlotRequest
import com.example.gieldadyzurowa.types.DoctorAvailability
import com.example.gieldadyzurowa.types.DutySlotActionRequest
import com.example.gieldadyzurowa.types.DutyVacancy
import com.example.gieldadyzurowa.types.LoginRequest
import com.example.gieldadyzurowa.types.PublishDutySlotRequest
import com.example.gieldadyzurowa.types.RegistrationRequest
import com.example.gieldadyzurowa.types.Specialty
import com.example.gieldadyzurowa.types.UserroleAndId

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("/auth/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<Unit>

    @GET("/user/data")
    fun fetchUserData(): Call<UserroleAndId>

    @POST("/auth/register")
    fun registerUser(@Body registrationRequest: RegistrationRequest): Call<Void>

    @GET("/duty/slots/json")
    suspend fun fetchDutyVacancies(): Response<List<DutyVacancy>>

    @POST("/duty/publish")
    suspend fun publishDutyVacancy(
        @Body dutyVacancy: PublishDutySlotRequest
    ): Response<Unit>

    @GET("/doctor/availabilities/json")
    suspend fun fetchDoctorAvailabilities(): Response<List<DoctorAvailability>>

    @POST("/doctor/availability")
    suspend fun addDoctorAvailability(
        @Body availabilityRequest: DoctorAvailability
    ): Response<Unit> // A

    @POST("/assign-duty-slot")
    suspend fun assignDutySlot(
        @Body assignDutySlotRequest: AssignDutySlotRequest
    ): Response<Unit>

    @POST("/give-consent")
    suspend fun giveConsent(
        @Body request: DutySlotActionRequest
    ): Response<Unit>

    @POST("/revoke-assignment")
    suspend fun revokeAssignment(
        @Body request: DutySlotActionRequest
    ): Response<Unit>

    @POST("/duty/remove")
    suspend fun removeDutySlot(
        @Body request: DutySlotActionRequest
    ): Response<Unit>

    @GET("/specialties")
    suspend fun getSpecialties(
    ): Response<List<Specialty>>
}
