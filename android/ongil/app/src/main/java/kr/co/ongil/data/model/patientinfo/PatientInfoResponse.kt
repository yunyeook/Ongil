package kr.co.ongil.data.model.patientinfo

import kotlinx.serialization.Serializable

@Serializable
data class PatientInfoResponse(
    val message: String,
    val data: PatientInfoDto
)
