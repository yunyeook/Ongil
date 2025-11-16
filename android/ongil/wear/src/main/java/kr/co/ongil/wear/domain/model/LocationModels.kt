package kr.co.ongil.wear.domain.model

/**
 * 위치 업데이트 Domain Model
 */
data class LocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 네비게이션 상태 Domain Model
 */
data class NavigationState(
    val navigationId: Long? = null,
    val isActive: Boolean = false,
    val startLocation: NavigationLocation? = null,
    val endLocation: NavigationLocation? = null,
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val distanceToDestination: Float? = null,
    val isDeviated: Boolean = false
)

/**
 * 네비게이션 위치 Domain Model
 */
data class NavigationLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String
)

/**
 * 안전 범위 설정 Domain Model
 */
data class SafeZoneConfig(
    val homeLatitude: Double,
    val homeLongitude: Double,
    val stage1Radius: Int = 100,     // 100m
    val stage2Radius: Int = 350,     // 350m
    val stage3Radius: Int = 700,     // 700m
    val stage1ThresholdMinutes: Int = 60,
    val stage2ThresholdMinutes: Int = 30,
    val stage3ThresholdMinutes: Int = 15
)

/**
 * 안전 범위 상태 Domain Model
 */
data class SafeZoneStatus(
    val isInsideStage1: Boolean = true,
    val isInsideStage2: Boolean = true,
    val isInsideStage3: Boolean = true,
    val stage1OutsideDuration: Long? = null,
    val stage2OutsideDuration: Long? = null,
    val stage3OutsideDuration: Long? = null,
    val hasAlerted: Boolean = false
)
