package kr.co.ongil.domain.patient.insight.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.ongil.domain.patient.dashboard.repository.DashboardRepository;
import kr.co.ongil.domain.patient.insight.dto.internal.*;
import kr.co.ongil.domain.patient.insight.entity.PatientInsight;
import kr.co.ongil.domain.patient.insight.entity.PeriodType;
import kr.co.ongil.domain.patient.insight.repository.PatientInsightRepository;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.repository.UserRepository;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 환자 인사이트 메인 서비스
 * 데이터 집계 → 플래그 평가 → LLM 인사이트 생성 → 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientInsightService {

    private final InsightAggregatorService aggregatorService;
    private final InsightFlagEvaluator flagEvaluator;
    private final GmsLLMClient gmsLLMClient;
    private final PatientInsightRepository insightRepository;
    private final DashboardRepository dashboardRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * 환자 인사이트 생성 (WEEKLY 또는 MONTHLY)
     */
    @Transactional
    public PatientInsight generateInsight(Integer patientId, PeriodType periodType) {
        log.info("환자 인사이트 생성 시작 - patientId: {}, periodType: {}", patientId, periodType);

        // 1. 환자 존재 확인
        User patient = userRepository.findById(patientId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_NOT_FOUND));

        // 2. 기간 정보 생성
        PeriodInfo period = createPeriodInfo(periodType);

        // 3. DashboardCalc 존재 확인 (데이터가 아예 없으면 에러)
        if (!hasDashboardData(patientId, period.currentStart())) {
            log.warn("DashboardCalc 데이터가 없어 인사이트 생성 불가 - patientId: {}", patientId);
            throw new BusinessException(ErrorCode.INSUFFICIENT_DATA_FOR_ANALYSIS);
        }

        // 4. 이미 생성된 인사이트가 있는지 확인
        PatientInsight existing = insightRepository
            .findByPatientIdAndPeriodTypeAndPeriodStartDateAndPeriodEndDate(
                patientId, periodType, period.currentStart(), period.currentEnd())
            .orElse(null);

        if (existing != null) {
            log.info("이미 생성된 인사이트 반환 - patientId: {}, periodType: {}", patientId, periodType);
            return existing;
        }

        // 5. 데이터 집계
        ActivityStats activity = aggregatorService.aggregateActivityStats(patientId, period);
        HealthStats health = aggregatorService.aggregateHealthStats(patientId, period);

        // 6. 플래그 평가
        InsightFlags flags = flagEvaluator.evaluateFlags(activity, health);

        // 7. PatientInsightFeatures 생성
        PatientInsightFeatures features = PatientInsightFeatures.builder()
            .patientId(patientId)
            .patientProfile(createPatientProfile(patient))
            .period(period)
            .activity(activity)
            .health(health)
            .flags(flags)
            .build();

        // 8. LLM 인사이트 생성
        LLMInsightResponse llmResponse = gmsLLMClient.generateInsight(features);

        // 9. PatientInsight 엔티티 생성 및 저장
        PatientInsight insight = PatientInsight.builder()
            .patientId(patientId)
            .periodType(periodType)
            .periodStartDate(period.currentStart())
            .periodEndDate(period.currentEnd())
            .overallRiskLevel(llmResponse.overallRiskLevel())
            .summary(llmResponse.summary())
            .positiveSignals(toJsonString(llmResponse.positiveSignals()))
            .warningSignals(toJsonString(llmResponse.warningSignals()))
            .possibleInterpretations(toJsonString(llmResponse.possibleInterpretations()))
            .caregiverSuggestions(toJsonString(llmResponse.caregiverSuggestions()))
            .dataNotes(toJsonString(llmResponse.dataNotes()))
            .inputFeatures(toJsonString(features))
            .llmRawResponse(toJsonString(llmResponse))
            .build();

        PatientInsight saved = insightRepository.save(insight);
        log.info("환자 인사이트 생성 완료 - insightId: {}, patientId: {}", saved.getId(), patientId);

        return saved;
    }

    /**
     * 환자 인사이트 조회 (최신 1건)
     */
    @Transactional(readOnly = true)
    public PatientInsight getLatestInsight(Integer patientId, PeriodType periodType) {
        return insightRepository
            .findFirstByPatientIdAndPeriodTypeOrderByPeriodEndDateDesc(patientId, periodType)
            .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_INSIGHT_NOT_FOUND));
    }

    /**
     * 환자 인사이트 조회 (특정 기간)
     */
    @Transactional(readOnly = true)
    public PatientInsight getInsightByPeriod(Integer patientId, PeriodType periodType,
                                               LocalDate startDate, LocalDate endDate) {
        return insightRepository
            .findByPatientIdAndPeriodTypeAndPeriodStartDateAndPeriodEndDate(
                patientId, periodType, startDate, endDate)
            .orElseThrow(() -> new BusinessException(ErrorCode.PATIENT_INSIGHT_NOT_FOUND));
    }

    /**
     * 환자 인사이트 목록 조회 (최근 N개)
     */
    @Transactional(readOnly = true)
    public List<PatientInsight> getInsightHistory(Integer patientId, PeriodType periodType, int limit) {
        return insightRepository
            .findByPatientIdAndPeriodTypeOrderByPeriodEndDateDesc(patientId, periodType)
            .stream()
            .limit(limit)
            .toList();
    }

    /**
     * 인사이트 재생성 (기존 데이터 삭제 후 재생성)
     */
    @Transactional
    public PatientInsight regenerateInsight(Integer patientId, PeriodType periodType,
                                             LocalDate startDate, LocalDate endDate) {
        log.info("인사이트 재생성 - patientId: {}, periodType: {}, period: {} ~ {}",
            patientId, periodType, startDate, endDate);

        // 기존 인사이트 삭제
        insightRepository.findByPatientIdAndPeriodTypeAndPeriodStartDateAndPeriodEndDate(
            patientId, periodType, startDate, endDate)
            .ifPresent(insightRepository::delete);

        // 새로 생성
        return generateInsight(patientId, periodType);
    }

    // ==================== 내부 헬퍼 메서드 ====================

    private PeriodInfo createPeriodInfo(PeriodType periodType) {
        return switch (periodType) {
            case WEEKLY -> PeriodInfo.thisWeekAndLastWeek();
            case MONTHLY -> PeriodInfo.thisMonthAndLastMonth();
        };
    }

    private boolean hasDashboardData(Integer patientId, LocalDate periodStart) {
        // DashboardCalc는 매주 갱신되므로, 해당 기간 이후의 데이터가 있는지 확인
        return dashboardRepository.existsByPatientId(patientId);
    }

    private PatientInsightFeatures.PatientProfile createPatientProfile(User patient) {
        // 나이 계산
        String ageGroup = "미상";
        if (patient.getBirth() != null && !patient.getBirth().isEmpty()) {
            try {
                // birth 형식: "19900101" 또는 "1990-01-01"
                String birthStr = patient.getBirth().replace("-", "");
                if (birthStr.length() >= 8) {
                    int year = Integer.parseInt(birthStr.substring(0, 4));
                    int currentYear = LocalDate.now().getYear();
                    int age = currentYear - year;
                    ageGroup = (age / 10) * 10 + "대";
                }
            } catch (Exception e) {
                log.warn("생년월일 파싱 실패 - patientId: {}, birth: {}", patient.getId(), patient.getBirth());
            }
        }

        // 성별 정보 없음 (User 엔티티에 gender 필드 없음)
        String gender = "UNKNOWN";

        return PatientInsightFeatures.PatientProfile.builder()
            .ageGroup(ageGroup)
            .gender(gender)
            .build();
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON 직렬화 실패: {}", e.getMessage(), e);
            return "[]";
        }
    }
}
