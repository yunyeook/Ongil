package kr.co.ongil.domain.model

/**
 * 건강 데이터 도메인 모델
 * (프레젠테이션 레이어와 데이터 레이어 간 중립적인 모델)
 * 시간대별 개별 레코드들을 담고 있음
 */
data class HealthData(
    val heartRateRecords: List<HeartRate>,
    val oxygenSaturationRecords: List<OxygenSaturation>,
    val sleepRecords: List<Sleep>,
    val stepsRecords: List<Steps>
)

data class HeartRate(
    val beatsPerMinute: Long,
    val measuredAt: String
)

data class OxygenSaturation(
    val percentage: Double,
    val measuredAt: String
)

data class Sleep(
    val durationHours: Double,
    val measuredAt: String
)

data class Steps(
    val count: Long,
    val measuredAt: String
)
