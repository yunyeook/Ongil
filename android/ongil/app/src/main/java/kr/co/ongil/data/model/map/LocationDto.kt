package kr.co.ongil.data.model.map

import kotlinx.serialization.Serializable

/**
 * 환자 위치 전송 Request
 * POST /api/v1/patients/{patientId}/location
 */
@Serializable
data class UpdateLocationRequest(
    val latitude: Double,
    val longitude: Double
)

/**
 * 환자 위치 전송 Response
 */
@Serializable
data class UpdateLocationResponse(
    val message: String,
    val data: String? = null
)
