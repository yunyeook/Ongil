package kr.co.ongil.domain.model

/**
 * 건강 데이터 타입
 */
enum class HealthDataType(val value: String) {
    HEART_RATE("HEART_RATE"),
    OXYGEN_SATURATION("OXYGEN_SATURATION"),
    SLEEP("SLEEP"),
    STEP_COUNT("STEP_COUNT"),
    BLOOD_PRESSURE_SYSTOLIC("BLOOD_PRESSURE_SYSTOLIC"),
    BLOOD_PRESSURE_DIASTOLIC("BLOOD_PRESSURE_DIASTOLIC"),
    BODY_TEMPERATURE("BODY_TEMPERATURE"),
    BLOOD_GLUCOSE("BLOOD_GLUCOSE"),
    BODY_WEIGHT("BODY_WEIGHT"),
    ACTIVE_ENERGY("ACTIVE_ENERGY"),
    RESTING_HEART_RATE("RESTING_HEART_RATE"),
    RESPIRATORY_RATE("RESPIRATORY_RATE"),
    STRESS_LEVEL("STRESS_LEVEL"),
    DISTANCE("DISTANCE"),
    EXERCISE_TIME("EXERCISE_TIME");

    companion object {
        fun fromValue(value: String): HealthDataType? {
            return entries.find { it.value == value }
        }
    }
}