package kr.co.ongil.data.model.websocket

import kotlinx.serialization.Serializable

/**
 * 좌표 정보
 */
@Serializable
data class CoordinateInfo(
    val latitude: Double,
    val longitude: Double
)

/**
 * GPS 위치 WebSocket 메시지
 *
 * type:
 * - "GPS_UPDATE": 환자가 자신의 위치를 서버로 전송
 * - "GPS_DISCONNECT": 환자가 GPS 추적 종료를 서버에 알림
 * - "CONNECTION_ACK": 서버가 연결 성공을 알림
 *
 * coordinate:
 * - GPS 좌표 정보 (latitude, longitude)
 * - type이 "CONNECTION_ACK" 또는 "GPS_DISCONNECT"일 경우 null
 */
@Serializable
data class GpsMessage(
    val type: String,
    val coordinate: CoordinateInfo? = null
)
