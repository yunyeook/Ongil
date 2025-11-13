package kr.co.ongil.domain.patient.insight.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.ongil.domain.patient.insight.dto.response.PatientInsightResponse;
import kr.co.ongil.domain.patient.insight.entity.PatientInsight;
import kr.co.ongil.domain.patient.insight.entity.PeriodType;
import kr.co.ongil.domain.patient.insight.service.PatientInsightService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 환자 인사이트 API 컨트롤러
 * AI 기반 주간/월간 종합 분석 리포트 제공
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/patients/{patientId}/insights")
@RequiredArgsConstructor
@Tag(name = "Patient Insight API", description = "환자 AI 인사이트 분석 API")
public class PatientInsightController {

    private final PatientInsightService insightService;

    /**
     * 주간 인사이트 생성
     */
    @PostMapping("/weekly")
    @Operation(
        summary = "주간 인사이트 생성",
        description = "환자의 주간 활동 및 건강 데이터를 분석하여 AI 인사이트를 생성합니다. 이미 생성된 인사이트가 있으면 기존 데이터를 반환합니다."
    )
    public ApiResponse<PatientInsightResponse> generateWeeklyInsight(
        @Parameter(description = "환자 ID", required = true, example = "10")
        @PathVariable Integer patientId
    ) {
        log.info("주간 인사이트 생성 요청 - patientId: {}", patientId);

        PatientInsight insight = insightService.generateInsight(patientId, PeriodType.WEEKLY);
        PatientInsightResponse response = PatientInsightResponse.from(insight);

        return ApiResponse.success(ResponseMessage.PATIENT_INSIGHT_GENERATED, response);
    }

    /**
     * 월간 인사이트 생성
     */
    @PostMapping("/monthly")
    @Operation(
        summary = "월간 인사이트 생성",
        description = "환자의 월간 활동 및 건강 데이터를 분석하여 AI 인사이트를 생성합니다. 이미 생성된 인사이트가 있으면 기존 데이터를 반환합니다."
    )
    public ApiResponse<PatientInsightResponse> generateMonthlyInsight(
        @Parameter(description = "환자 ID", required = true, example = "10")
        @PathVariable Integer patientId
    ) {
        log.info("월간 인사이트 생성 요청 - patientId: {}", patientId);

        PatientInsight insight = insightService.generateInsight(patientId, PeriodType.MONTHLY);
        PatientInsightResponse response = PatientInsightResponse.from(insight);

        return ApiResponse.success(ResponseMessage.PATIENT_INSIGHT_GENERATED, response);
    }

    /**
     * 최신 주간 인사이트 조회
     */
    @GetMapping("/weekly/latest")
    @Operation(
        summary = "최신 주간 인사이트 조회",
        description = "환자의 가장 최근 주간 인사이트를 조회합니다."
    )
    public ApiResponse<PatientInsightResponse> getLatestWeeklyInsight(
        @Parameter(description = "환자 ID", required = true, example = "10")
        @PathVariable Integer patientId
    ) {
        log.info("최신 주간 인사이트 조회 - patientId: {}", patientId);

        PatientInsight insight = insightService.getLatestInsight(patientId, PeriodType.WEEKLY);
        PatientInsightResponse response = PatientInsightResponse.from(insight);

        return ApiResponse.success(ResponseMessage.PATIENT_INSIGHT_FOUND, response);
    }

    /**
     * 최신 월간 인사이트 조회
     */
    @GetMapping("/monthly/latest")
    @Operation(
        summary = "최신 월간 인사이트 조회",
        description = "환자의 가장 최근 월간 인사이트를 조회합니다."
    )
    public ApiResponse<PatientInsightResponse> getLatestMonthlyInsight(
        @Parameter(description = "환자 ID", required = true, example = "10")
        @PathVariable Integer patientId
    ) {
        log.info("최신 월간 인사이트 조회 - patientId: {}", patientId);

        PatientInsight insight = insightService.getLatestInsight(patientId, PeriodType.MONTHLY);
        PatientInsightResponse response = PatientInsightResponse.from(insight);

        return ApiResponse.success(ResponseMessage.PATIENT_INSIGHT_FOUND, response);
    }

    /**
     * 주간 인사이트 이력 조회
     */
    @GetMapping("/weekly")
    @Operation(
        summary = "주간 인사이트 이력 조회",
        description = "환자의 주간 인사이트 이력을 최근순으로 조회합니다."
    )
    public ApiResponse<List<PatientInsightResponse>> getWeeklyInsightHistory(
        @Parameter(description = "환자 ID", required = true, example = "10")
        @PathVariable Integer patientId,

        @Parameter(description = "조회할 개수", example = "10")
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("주간 인사이트 이력 조회 - patientId: {}, limit: {}", patientId, limit);

        List<PatientInsight> insights = insightService.getInsightHistory(patientId, PeriodType.WEEKLY, limit);
        List<PatientInsightResponse> response = insights.stream()
            .map(PatientInsightResponse::from)
            .toList();

        return ApiResponse.success(ResponseMessage.PATIENT_INSIGHT_LIST_FOUND, response);
    }

    /**
     * 월간 인사이트 이력 조회
     */
    @GetMapping("/monthly")
    @Operation(
        summary = "월간 인사이트 이력 조회",
        description = "환자의 월간 인사이트 이력을 최근순으로 조회합니다."
    )
    public ApiResponse<List<PatientInsightResponse>> getMonthlyInsightHistory(
        @Parameter(description = "환자 ID", required = true, example = "10")
        @PathVariable Integer patientId,

        @Parameter(description = "조회할 개수", example = "10")
        @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("월간 인사이트 이력 조회 - patientId: {}, limit: {}", patientId, limit);

        List<PatientInsight> insights = insightService.getInsightHistory(patientId, PeriodType.MONTHLY, limit);
        List<PatientInsightResponse> response = insights.stream()
            .map(PatientInsightResponse::from)
            .toList();

        return ApiResponse.success(ResponseMessage.PATIENT_INSIGHT_LIST_FOUND, response);
    }
}
