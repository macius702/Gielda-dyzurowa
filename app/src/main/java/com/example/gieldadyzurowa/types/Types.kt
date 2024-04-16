package com.example.gieldadyzurowa.types

data class Hospital(
    val _id: String,
    val username: String,
    val password: String, // TODO(mtlk) Note: It's unusual and insecure to handle passwords in client-side code.
    val role: String,
    val profileVisible: Boolean
)


data class Doctor(
    val _id: String,
    val username: String,
    val password: String, // TODO(mtlk) Note: Handling passwords like this is insecure, especially on client-side.
    val role: String,
    val specialty: String,
    val localization: String,
    val profileVisible: Boolean
)


data class DoctorAvailability(
    val doctorId: Doctor?, val date: String, val availableHours: String
)


data class DutyVacancy(
    val _id: String?,
    val hospitalId: Hospital?,
    val date: String,
    val dutyHours: String,
    val requiredSpecialty: String,
    val status: DutySlotStatus, // Use enum type here
    val assignedDoctorId: Doctor? = null
)

enum class DutySlotStatus(val status: String) {
    OPEN("open"), PENDING("pending"), FILLED("filled");

    companion object {
        fun from(status: String): DutySlotStatus {
            return when (status) {
                "open" -> OPEN
                "pending" -> PENDING
                "filled" -> FILLED
                else -> throw IllegalArgumentException("Unknown status: $status")
            }
        }
    }
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
    val _id: String, val role: String
)

data class AssignDutySlotRequest(
    val _id: String,
    val sendingDoctorId: String,
)

data class DutySlotActionRequest(
    val _id: String
)
