package kr.co.ongil.domain.model

/**
 * 건강 데이터 도메인 모델
 * (프레젠테이션 레이어와 데이터 레이어 간 중립적인 모델)
 */
data class HealthData(
    val heartRate: HeartRate?,
    val oxygenSaturation: OxygenSaturation?,
    val sleep: Sleep?,
    val steps: Steps?
)

data class HeartRate(
    val average: Long,
    val max: Long,
    val min: Long
)

data class OxygenSaturation(
    val average: Double,
    val max: Double,
    val min: Double
)

data class Sleep(
    val average: Double, // 시간 단위
    val max: Double,
    val min: Double
)

data class Steps(
    val average: Long,
    val max: Long,
    val min: Long
)
