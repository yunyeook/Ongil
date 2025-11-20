package kr.co.ongil.data.model.health

/**
 * Health Connect에서 가져온 로컬 건강 데이터
 * (API 응답 모델인 HealthDataResponse의 HealthData와 구분하기 위해 LocalHealthData로 명명)
 * 시간대별 개별 레코드들을 담고 있음
 */
data class LocalHealthData(
    val heartRateRecords: List<HeartRateRecord>,
    val oxygenSaturationRecords: List<OxygenSaturationRecord>,
    val sleepRecords: List<SleepRecord>,
    val stepsRecords: List<StepsRecord>
)

/**
 * 심박수 개별 레코드
 */
data class HeartRateRecord(
    val beatsPerMinute: Long,
    val measuredAt: String // ISO-8601 형식: "2025-10-18T14:22:00"
)

/**
 * 혈중산소포화도 개별 레코드
 */
data class OxygenSaturationRecord(
    val percentage: Double,
    val measuredAt: String
)

/**
 * 수면 개별 레코드
 */
data class SleepRecord(
    val durationHours: Double, // 시간 단위
    val measuredAt: String
)

/**
 * 걸음수 개별 레코드
 */
data class StepsRecord(
    val count: Long,
    val measuredAt: String
)
