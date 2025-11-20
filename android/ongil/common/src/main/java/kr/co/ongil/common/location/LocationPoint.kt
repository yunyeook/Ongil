package kr.co.ongil.common.location

/**
 * 위치 정보 데이터 모델
 * - FusedLocationProviderClient 결과를 그대로 매핑
 */
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float?,
    val bearing: Float?,
    val speedMps: Float?,
    val timeMillis: Long
)