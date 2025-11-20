package kr.co.ongil.data.model.patientinfo

import kotlinx.serialization.Serializable

@Serializable
data class PatientInfoDto(
    val favorite: String,
    val safezoneExit: String,
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

@Serializable
data class TimeSlotRisk(
    val timeRange: String,      // "00-06시"
    val intensity: Float         // 0.0 ~ 1.0
)

@Serializable
data class DailyRiskCount(
    val date: String,            // "2025-11-10"
    val totalCount: Long         // 위험 행동 횟수
)
