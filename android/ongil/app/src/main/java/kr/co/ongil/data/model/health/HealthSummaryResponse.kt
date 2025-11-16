package kr.co.ongil.data.model.health

import kotlinx.serialization.Serializable

/**
 * 생체 데이터 요약 통계 조회 응답
 */
@Serializable
data class HealthSummaryResponse(
    val message: String,
    val data: HealthSummaryData
)

@Serializable
data class HealthSummaryData(
    val patientId: Long,
    val type: String,
    val unit: String,
    val summary: List<HealthSummaryRecord>
)

@Serializable
data class HealthSummaryRecord(
    val date: String,
    val average: Double,
    val max: Double,
    val min: Double,
    val count: Int
)
