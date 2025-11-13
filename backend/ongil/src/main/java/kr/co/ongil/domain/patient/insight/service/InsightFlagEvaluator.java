package kr.co.ongil.domain.patient.insight.service;

import kr.co.ongil.domain.patient.insight.dto.internal.ActivityStats;
import kr.co.ongil.domain.patient.insight.dto.internal.HealthStats;
import kr.co.ongil.domain.patient.insight.dto.internal.InsightFlags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 환자 인사이트 플래그 평가 서비스
 * 규칙 기반으로 6가지 플래그를 판단
 */
@Slf4j
@Service
public class InsightFlagEvaluator {

    // ==================== 임계값 상수 ====================

    private static final double ROUTINE_CHANGE_THRESHOLD = 30.0;  // 일상 변화 비율 (%)
    private static final int SAFEZONE_EXIT_THRESHOLD = 3;  // 안전구역 이탈 횟수
    private static final int WANDER_THRESHOLD = 2;  // 배회 횟수
    private static final int ROUTE_LOST_THRESHOLD = 2;  // 길 잃음 횟수
    private static final double ANXIETY_ESCALATION_THRESHOLD = 50.0;  // 불안 증가율 (%)
    private static final int EMERGENCY_INCREASE_THRESHOLD = 2;  // 긴급 상황 증가 횟수
    private static final double SLEEP_DECREASE_THRESHOLD = 1.0;  // 수면 감소 시간 (시간)
    private static final double STEP_DECREASE_THRESHOLD = 20.0;  // 걸음수 감소율 (%)
    private static final int SOS_NOT_RESPONDED_THRESHOLD = 1;  // 미응답 SOS 횟수

    /**
     * 활동 및 건강 통계를 기반으로 6가지 플래그 평가
     */
    public InsightFlags evaluateFlags(ActivityStats activity, HealthStats health) {
        log.info("플래그 평가 시작");

        boolean routineChange = evaluateRoutineChange(activity, health);
        boolean spatialConfusion = evaluateSpatialConfusion(activity);
        boolean anxietyEscalation = evaluateAnxietyEscalation(activity);
        boolean physicalDrop = evaluatePhysicalConditionDrop(health);
        boolean sleepActivityCorr = evaluateSleepActivityCorrelation(activity, health);
        boolean panicResponse = evaluatePanicResponsePattern(activity);

        InsightFlags flags = new InsightFlags(
            routineChange,
            spatialConfusion,
            anxietyEscalation,
            physicalDrop,
            sleepActivityCorr,
            panicResponse
        );

        log.info("플래그 평가 완료 - activeCount: {}, estimatedRisk: {}",
            flags.activeCount(), flags.estimateRiskLevel());

        return flags;
    }

    /**
     * 1. 일상 패턴 변화 감지
     * - 자주 가는 장소의 변화가 큰 경우
     * - 안전구역 이탈 횟수가 급증한 경우
     * - 배회 횟수가 증가한 경우
     */
    private boolean evaluateRoutineChange(ActivityStats activity, HealthStats health) {
        // 자주 가는 장소 다양성이 낮은 경우 (루틴이 단조로워짐)
        boolean lowDiversity = activity.favorite().diversityIndex() < 0.3;

        // 안전구역 이탈 급증
        int safezoneIncrease = activity.safezoneExit().current() - activity.safezoneExit().previous();
        boolean safezoneSpike = safezoneIncrease >= SAFEZONE_EXIT_THRESHOLD;

        // 배회 증가
        int wanderIncreaseCount = activity.wander().current() - activity.wander().previous();
        boolean wanderIncreased = wanderIncreaseCount >= WANDER_THRESHOLD;

        // 걸음수 패턴 변화 (급증 또는 급감)
        boolean stepChange = health.steps().trend().equals("INCREASE") ||
            health.steps().trend().equals("DECREASE");
        boolean significantStepChange = Math.abs(health.steps().changeRate()) > ROUTINE_CHANGE_THRESHOLD;

        return (lowDiversity && safezoneSpike) || wanderIncreased || (stepChange && significantStepChange);
    }

    /**
     * 2. 공간 혼란 감지
     * - 길 잃음 횟수가 많은 경우
     * - 배회 시간이 긴 경우
     * - 안전구역 이탈이 반복되는 경우
     */
    private boolean evaluateSpatialConfusion(ActivityStats activity) {
        // 길 잃음 빈도
        boolean frequentRouteLost = activity.route().current() >= ROUTE_LOST_THRESHOLD;

        // 배회 시간이 긴 경우 (평균 10분 이상)
        boolean longWander = activity.wander().avgDurationMinutes() > 10.0;

        // 안전구역 이탈 빈도
        boolean frequentSafezoneExit = activity.safezoneExit().current() >= SAFEZONE_EXIT_THRESHOLD;

        // 야간 배회
        boolean nightWander = activity.wander().nightOccurrences() > 0;

        return frequentRouteLost || (longWander && frequentSafezoneExit) || nightWander;
    }

    /**
     * 3. 불안/위험 증가 감지
     * - 긴급 통화/SOS 빈도 증가
     * - 야간 외출 증가
     * - 안전구역 이탈 빈도 증가
     */
    private boolean evaluateAnxietyEscalation(ActivityStats activity) {
        // 긴급 상황 증가
        int emerCallIncrease = activity.emergency().emerCallCurrent() - activity.emergency().emerCallPrevious();
        int sosSignIncrease = activity.emergency().sosSignCurrent() - activity.emergency().sosSignPrevious();
        boolean emergencyIncrease = (emerCallIncrease + sosSignIncrease) >= EMERGENCY_INCREASE_THRESHOLD;

        // 미응답 SOS
        boolean unrespondedSos = activity.emergency().sosNotResponded() >= SOS_NOT_RESPONDED_THRESHOLD;

        // 안전구역 이탈 급증률
        int safezoneIncrease = activity.safezoneExit().current() - activity.safezoneExit().previous();
        double increaseRate = activity.safezoneExit().previous() == 0 ? 0.0 :
            (double) safezoneIncrease / activity.safezoneExit().previous() * 100.0;
        boolean safezoneEscalation = increaseRate > ANXIETY_ESCALATION_THRESHOLD;

        // 야간 외출/배회
        boolean nightActivity = activity.favorite().nightOutingCount() > 0 ||
            activity.wander().nightOccurrences() > 0;

        return emergencyIncrease || unrespondedSos || safezoneEscalation || nightActivity;
    }

    /**
     * 4. 신체 상태 저하 감지
     * - 수면 시간 감소
     * - 걸음수 감소
     * - 심박수 변동성 증가
     * - 산소포화도 감소
     */
    private boolean evaluatePhysicalConditionDrop(HealthStats health) {
        // 데이터 가용성 확인
        if (!health.dataAvailability().sleep() && !health.dataAvailability().steps()) {
            return false;  // 데이터가 없으면 판단 불가
        }

        // 수면 감소
        boolean sleepDecrease = health.sleep().trend().equals("DECREASE") &&
            health.sleep().avgHoursCurrent() != null &&
            health.sleep().avgHoursPrevious() != null &&
            (health.sleep().avgHoursPrevious() - health.sleep().avgHoursCurrent()) > SLEEP_DECREASE_THRESHOLD;

        // 걸음수 감소
        boolean stepDecrease = health.steps().trend().equals("DECREASE") &&
            health.steps().changeRate() < -STEP_DECREASE_THRESHOLD;

        // 심박수 변동성 증가 (30 이상)
        boolean highHeartRateVariability = health.heartRate().variabilityCurrent() != null &&
            health.heartRate().variabilityCurrent() > 30.0;

        // 산소포화도 저하 (95% 미만)
        boolean lowOxygen = health.oxygenSaturation().avgCurrent() != null &&
            health.oxygenSaturation().avgCurrent() < 95.0;

        return sleepDecrease || stepDecrease || highHeartRateVariability || lowOxygen;
    }

    /**
     * 5. 수면-활동 상관관계 감지
     * - 수면 부족과 활동량 감소의 상관관계
     * - 수면 부족과 배회/이탈 증가의 상관관계
     */
    private boolean evaluateSleepActivityCorrelation(ActivityStats activity, HealthStats health) {
        if (!health.dataAvailability().sleep()) {
            return false;  // 수면 데이터 없으면 판단 불가
        }

        // 수면 부족 (평균 6시간 미만)
        boolean sleepDeprivation = health.sleep().avgHoursCurrent() != null &&
            health.sleep().avgHoursCurrent() < 6.0;

        if (!sleepDeprivation) {
            return false;  // 수면 부족이 아니면 상관관계 없음
        }

        // 활동량 감소와 수면 부족
        boolean lowActivityWithSleepIssue = health.steps().trend().equals("DECREASE") &&
            health.steps().changeRate() < -15.0;

        // 배회/이탈 증가와 수면 부족
        boolean spatialIssueWithSleepIssue = activity.wander().current() > activity.wander().previous() ||
            activity.safezoneExit().current() > activity.safezoneExit().previous();

        return lowActivityWithSleepIssue || spatialIssueWithSleepIssue;
    }

    /**
     * 6. 패닉 반응 패턴 감지
     * - 짧은 시간 내 여러 긴급 상황 발생
     * - 미응답 SOS가 많은 경우
     * - 야간 긴급 상황 발생
     */
    private boolean evaluatePanicResponsePattern(ActivityStats activity) {
        // 긴급 상황 빈도가 높은 경우 (3회 이상)
        int totalEmergency = activity.emergency().emerCallCurrent() + activity.emergency().sosSignCurrent();
        boolean frequentEmergency = totalEmergency >= 3;

        // 미응답 SOS
        boolean unrespondedSos = activity.emergency().sosNotResponded() >= 2;

        // 야간 배회와 긴급 상황의 조합
        boolean nightPanic = activity.wander().nightOccurrences() > 0 && totalEmergency > 0;

        return frequentEmergency || unrespondedSos || nightPanic;
    }
}
