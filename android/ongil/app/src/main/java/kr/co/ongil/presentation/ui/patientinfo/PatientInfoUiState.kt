package kr.co.ongil.presentation.ui.patientinfo

import kotlinx.serialization.Serializable

data class PatientInfoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val activityLog: ActivityLog? = null
)

// 활동 기록
data class ActivityLog(
    val favoriteLocations: List<FavoriteLocation>,
    val safezoneExit: Map<String, Int>,
    val routeLost: Long,
    val routeLostDiff: Long,
    val routeTransition: String,
    val safezoneEmer: Long,
    val safezoneEmerDiff: Long,
    val safezoneTransition: String,
    val sosSign: Long,
    val sosSignDiff: Long,
    val sosSignTransition: String,
    val emerCall: Long,
    val emerCallDiff: Long,
    val emerCallTransition: String
)

// 가장 많이 찾은 목적지
@Serializable
data class FavoriteLocation(
    val rank: Int,
    val placeName: String,
    val placeCount: Int
)
