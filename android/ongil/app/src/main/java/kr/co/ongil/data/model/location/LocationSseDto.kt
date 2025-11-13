package kr.co.ongil.data.model.location

import kotlinx.serialization.Serializable

/**
 * SSE 이벤트 통합 타입
 */
sealed interface SseEvent {
    data class GpsUpdate(val data: GpsUpdateEvent) : SseEvent
    data class NavigationUpdate(val data: NavigationUpdateEvent) : SseEvent
    data object Connected : SseEvent
}

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

/**
 * SSE로 받는 길찾기 업데이트 데이터
 * event: navigation-update
 */
@Serializable
data class NavigationUpdateEvent(
    val patientId: Long,
    val initiatorId: Long,
    val userType: String,
    val route: NavigationRoute?,  // null이면 종료
    val status: String  // "STARTED" | "ENDED"
)

@Serializable
data class NavigationRoute(
    val startLocation: Location,
    val endLocation: Location,
    val totalDistance: Int,  // 미터
    val totalTime: Int,  // 초
    val path: List<Coordinate>,
    val guides: List<NavigationGuide>
)

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String
)

@Serializable
data class NavigationGuide(
    val index: Int,
    val instruction: String,
    val distance: Int,  // 미터
    val time: Int,  // 초
    val turnTypeCode: Int,
    val turnType: String,
    val roadName: String
)
