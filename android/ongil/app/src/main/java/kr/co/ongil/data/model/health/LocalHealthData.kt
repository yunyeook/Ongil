package kr.co.ongil.data.model.health

/**
 * Health Connect에서 가져온 로컬 건강 데이터
 * (API 응답 모델인 HealthDataResponse의 HealthData와 구분하기 위해 LocalHealthData로 명명)
 */
data class LocalHealthData(
    val heartRate: HeartRateData?,
    val oxygenSaturation: OxygenSaturationData?,
    val sleep: SleepData?,
    val steps: StepsData?
)

data class HeartRateData(
    val average: Long,
    val max: Long,
    val min: Long
)

data class OxygenSaturationData(
    val average: Double,
    val max: Double,
    val min: Double
)

data class SleepData(
    val average: Double, // 시간 단위
    val max: Double,
    val min: Double
)

data class StepsData(
    val average: Long,
    val max: Long,
    val min: Long
)
