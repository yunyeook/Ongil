package kr.co.ongil.data.model.health

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 건강 데이터 업로드 요청
 */
@Serializable
data class HealthDataUploadRequest(
    @SerialName("records")
    val records: List<HealthDataRecordRequest>
)

/**
 * 단일 건강 데이터 레코드
 */
@Serializable
data class HealthDataRecordRequest(
    @SerialName("type")
    val type: String,           // "HEART_RATE", "OXYGEN_SATURATION", "SLEEP", "STEP_COUNT"

    @SerialName("average")
    val average: Double,

    @SerialName("max")
    val max: Double,

    @SerialName("min")
    val min: Double,

    @SerialName("unit")
    val unit: String,           // "bpm", "%", "hours", "steps"

    @SerialName("measuredAt")
    val measuredAt: String      // ISO-8601 형식: "2025-10-18T14:22:00"
)
