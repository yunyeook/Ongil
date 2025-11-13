package kr.co.ongil.data.model.health

data class HealthData(
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
