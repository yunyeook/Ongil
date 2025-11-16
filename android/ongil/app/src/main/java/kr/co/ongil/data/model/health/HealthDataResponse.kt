package kr.co.ongil.data.model.health

import kotlinx.serialization.Serializable

/**
 * 생체 데이터 조회 응답
 */
@Serializable
data class HealthDataResponse(
    val message: String,
    val data: HealthData
)

@Serializable
data class HealthData(
    val patientId: Long,
    val type: String,
    val records: List<HealthRecord>
)

@Serializable
data class HealthRecord(
    val recordId: Long,
    val type: String,
    val average: Double,
    val max: Double,
    val min: Double,
    val unit: String,
    val measuredAt: String
)