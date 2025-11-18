package kr.co.ongil.wear.data.model

/**
 * Phone으로부터 수신하는 네비게이션 경로 데이터
 *
 * Phone의 WearDataClient가 전송하는 데이터와 동일한 구조
 */
data class WearNavigationData(
    val navigationId: String,
    val startLocationName: String,
    val endLocationName: String,
    val totalDistanceMeters: Int,
    val totalTimeMinutes: Int,
    val routePath: String // JSON 형식: [{"lat":37.5,"lon":127.0},...]
)
