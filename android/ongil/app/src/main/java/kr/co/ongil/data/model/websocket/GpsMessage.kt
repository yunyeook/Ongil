package kr.co.ongil.data.model.websocket

import kotlinx.serialization.Serializable

/**
 * GPS 위치 WebSocket 메시지
 */
@Serializable
data class GpsMessage(
    val latitude: Double,
    val longitude: Double,
    val timestamp: String? = null
)
