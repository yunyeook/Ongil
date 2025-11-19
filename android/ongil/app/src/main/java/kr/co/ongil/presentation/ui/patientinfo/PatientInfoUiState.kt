package kr.co.ongil.presentation.ui.patientinfo

import kotlinx.serialization.Serializable
import kr.co.ongil.data.model.health.LocalHealthData

data class PatientInfoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val activityLog: ActivityLog? = null,
    val summary: String = "",
    val positiveSignals: List<String> = emptyList(),
    val warningSignals: List<String> = emptyList(),
    val caregiverSuggestions: List<String> = emptyList(),
    val userType: String = "",
    val healthData: LocalHealthData? = null,
    val healthPermissionGranted: Boolean = false,
    val isLoadingHealthData: Boolean = false,
    val healthSyncMessage: String? = null,
    // 서버에서 가져온 건강 데이터 (보호자용)
    val serverHealthData: ServerHealthData? = null
)

// 서버에서 가져온 건강 데이터
data class ServerHealthData(
    val heartRate: HealthStat? = null,
    val oxygenSaturation: HealthStat? = null,
    val sleep: HealthStat? = null,
    val steps: HealthStat? = null
)

data class HealthStat(
    val average: Double,
    val max: Double,
    val min: Double,
    val unit: String
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
    val emerCallTransition: String,
    // 🆕 시간대별 위험도 데이터
    val timeSlotRisks: List<TimeSlotRisk> = emptyList(),
    // 🆕 일별 위험 행동 누적 데이터
    val dailyRiskCounts: List<DailyRiskCount> = emptyList()
)

// 🆕 시간대별 위험도
data class TimeSlotRisk(
    val timeRange: String,      // "00-06시"
    val intensity: Float         // 0.0 ~ 1.0
)

// 🆕 일별 위험 행동 횟수
data class DailyRiskCount(
    val date: String,            // "2025-11-10"
    val totalCount: Long         // 위험 행동 횟수
)

// 가장 많이 찾은 목적지
@Serializable
data class FavoriteLocation(
    val rank: Int,
    val placeName: String,
    val placeCount: Int
)
