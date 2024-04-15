package com.example.gieldadyzurowa.network

import com.example.gieldadyzurowa.DoctorAvailability
import com.example.gieldadyzurowa.DutyVacancy
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("/auth/login")
    fun loginUser(@Body loginRequest: LoginRequest): Call<Unit> // Or Call<ResponseType> if your API returns a response

    @GET("/user/data")
    fun fetchUserData(): Call<UserroleAndId>

    @POST("/auth/register")
    fun registerUser(@Body registrationRequest: RegistrationRequest): Call<Void>

    @GET("/duty/slots/json")
    suspend fun fetchDutyVacancies(): Response<List<DutyVacancy>>

    @POST("duty/publish")
    suspend fun publishDutyVacancy(
        @Body dutyVacancy: DutyVacancy
    ): Response<Unit> // Adjust return type based on your API response

    @GET("/doctor/availabilities/json")
    suspend fun fetchDoctorAvailabilities(): Response<List<DoctorAvailability>>

    @POST("/doctor/availability")
    suspend fun addDoctorAvailability(
        @Body availabilityRequest: DoctorAvailability
    ): Response<Unit> // A

    @POST("/assign-duty-slot")
    suspend fun assignDutySlot(
        @Body assignDutySlotRequest: AssignDutySlotRequest
    ): Response<Unit> // Adjust the return type as needed based on your API response

    @POST("/give-consent")
    suspend fun giveConsent(
        @Body request: DutySlotActionRequest
    ): Response<Unit> // Adjust the return type as needed

    @POST("/revoke-assignment")
    suspend fun revokeAssignment(
        @Body request: DutySlotActionRequest
    ): Response<Unit> // Adjust the return type as needed

    @POST("/duty/remove")
    suspend fun removeDutySlot(
        @Body request: DutySlotActionRequest
    ): Response<Unit> 


}



data class LoginRequest(val username: String, val password: String)
data class RegistrationRequest(
    val username: String,
    val password: String,
    val role: String,
    val specialty: String?,
    val localization: String?
)


data class UserroleAndId(
    val _id: String,
    val role : String
    // Add other fields that your backend might return
)

data class AssignDutySlotRequest(
    val _id: String,
    val sendingDoctorId: String,
)

data class DutySlotActionRequest(
    val _id: String
)

