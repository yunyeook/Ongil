package kr.co.ongil.data.model.patientinfo

import kotlinx.serialization.Serializable

@Serializable
data class PatientInfoDto(
    val favorite: String,
    val safezoneExit: String,
    val routeLost: Long,
    val routeLostDiff: Long,
    val routeTransition: String,
    val safezoneEmer: Long,
    val safezoneEmerDiff: Long,
    val safezoneTransition: String,
    val sosSign: Long,
    val sosSignDiff: Long,
    val sosSignTransition: String,
    val emerCall: Long,
    val emerCallDiff: Long,
    val emerCallTransition: String
)
