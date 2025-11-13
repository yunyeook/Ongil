package kr.co.ongil.data.model.location

import kotlinx.serialization.Serializable

/**
 * SSE로 받는 GPS 업데이트 데이터
 * event: gps-update
 */
@Serializable
data class GpsUpdateEvent(
    val patientId: Long,
    val coordinate: Coordinate
)

@Serializable
data class Coordinate(
    val latitude: Double,
    val longitude: Double
)
