package kr.co.ongil.wear.data.model.sos

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * SOS 알림 전송 요청 DTO
 * POST /api/v1/patients/{patientId}/sos
 */
@Serializable
data class SendSosAlertRequest(
    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double,

    @SerialName("message")
    val message: String? = null
)

/**
 * SOS 알림 전송 응답 DTO
 */
@Serializable
data class SendSosAlertResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: SosAlertData
)

/**
 * SOS 알림 데이터 DTO
 */
@Serializable
data class SosAlertData(
    @SerialName("sosId")
    val sosId: Long,

    @SerialName("patientId")
    val patientId: Int,

    @SerialName("latitude")
    val latitude: Double,

    @SerialName("longitude")
    val longitude: Double,

    @SerialName("status")
    val status: String,

    @SerialName("createdAt")
    val createdAt: String
)

/**
 * SOS 알림 종료 응답 DTO
 * DELETE /api/v1/patients/{patientId}/sos
 */
@Serializable
data class StopSosAlertResponse(
    @SerialName("message")
    val message: String,

    @SerialName("data")
    val data: String? = null
)
