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
    val type: String?, // null 가능 (전체 타입 조회 시)
    val unit: String?, // null 가능 (데이터가 없을 때)
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
