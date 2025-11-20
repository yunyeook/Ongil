package kr.co.ongil.presentation.ui.safezonesetting

/**
 * 안전구역 설정 화면 UI 상태
 */
data class SafeZoneUiState(
    // 각 단계별 설정
    val level1: SafeZoneLevelConfig = SafeZoneLevelConfig(
        distanceMeters = 100,
        dwellMinutes = 60
    ),
    val level2: SafeZoneLevelConfig = SafeZoneLevelConfig(
        distanceMeters = 350,
        dwellMinutes = 30
    ),
    val level3: SafeZoneLevelConfig = SafeZoneLevelConfig(
        distanceMeters = 700,
        dwellMinutes = 15
    ),

    // 알림 설정
    val pushEnabled: Boolean = true,
    val autoCallEnabled: Boolean = false,

    // 긴급연락처
    val primaryContactName: String = "",
    val primaryContactRelation: String = "",
    val primaryContactPhone: String = "",

    // 지도 표시 설정
    val showOnMap: Boolean = true,
    val opacityEnabled: Boolean = false,

    // 상태
    val isLoading: Boolean = false,
    val isSavable: Boolean = true,
    val error: String? = null
)

/**
 * 각 단계별 안전구역 설정
 */
data class SafeZoneLevelConfig(
    val distanceMeters: Int,  // 안전 거리 (미터)
    val dwellMinutes: Int      // 배회 탐지 시간 (분)
)
