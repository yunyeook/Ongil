package kr.co.ongil.domain.patient.health.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 건강 데이터 타입 enum
 * Samsung Health SDK에서 가져올 수 있는 생체 데이터 종류 정의
 */
@Getter
@RequiredArgsConstructor
public enum HealthDataType {

    // ===== 기본 4대장 (현재 구현) =====

    /**
     * 심박수
     */
    HEART_RATE("심박수", "bpm"),

    /**
     * 혈중 산소포화도
     */
    OXYGEN_SATURATION("혈중 산소 포화도", "%"),

    /**
     * 수면 시간 (시간 단위)
     */
    SLEEP("수면 시간", "hours"),

    /**
     * 걸음 수
     */
    STEP_COUNT("걸음 수", "steps"),

    // ===== 확장 가능한 추가 타입들 =====

    /**
     * 수축기 혈압
     */
    BLOOD_PRESSURE_SYSTOLIC("수축기 혈압", "mmHg"),

    /**
     * 이완기 혈압
     */
    BLOOD_PRESSURE_DIASTOLIC("이완기 혈압", "mmHg"),

    /**
     * 체온
     */
    BODY_TEMPERATURE("체온", "°C"),

    /**
     * 혈당
     */
    BLOOD_GLUCOSE("혈당", "mg/dL"),

    /**
     * 체중
     */
    BODY_WEIGHT("체중", "kg"),

    /**
     * 활동 칼로리
     */
    ACTIVE_ENERGY("활동 칼로리", "kcal"),

    /**
     * 휴식시 심박수
     */
    RESTING_HEART_RATE("휴식시 심박수", "bpm"),

    /**
     * 호흡수
     */
    RESPIRATORY_RATE("호흡수", "breaths/min"),

    /**
     * 스트레스 지수
     */
    STRESS_LEVEL("스트레스 지수", "score"),

    /**
     * 이동 거리
     */
    DISTANCE("이동 거리", "km"),

    /**
     * 운동 시간
     */
    EXERCISE_TIME("운동 시간", "minutes");

    private final String description;
    private final String defaultUnit;
}
