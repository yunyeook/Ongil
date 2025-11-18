package kr.co.ongil.domain.patient.insight.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.ongil.domain.patient.insight.dto.internal.InsightFlags;
import kr.co.ongil.domain.patient.insight.dto.internal.LLMInsightResponse;
import kr.co.ongil.domain.patient.insight.dto.internal.PatientInsightFeatures;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * SSAFY GMS (Generative Model Service) API 클라이언트
 * LLM을 사용하여 환자 인사이트 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GmsLLMClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("classpath:insight/domain_guidelines.json")
    private Resource guidelinesResource;

    // 도메인 가이드라인 캐시
    private Map<String, Object> domainGuidelines;

    @Value("${gms.api.key:}")
    private String gmsApiKey;

    @Value("${gms.api.provider:openai}")
    private String gmsProvider;

    @Value("${gms.api.model:gpt-4.1-mini}")
    private String gmsModel;

    @Value("${gms.api.timeout:90}")
    private int timeoutSeconds;

    // OpenAI Provider 설정
    @Value("${gms.api.providers.openai.base-url:https://gms.ssafy.io/gmsapi/api.openai.com/v1}")
    private String openaiBaseUrl;

    @Value("${gms.api.providers.openai.endpoint:/chat/completions}")
    private String openaiEndpoint;

    // Gemini Provider 설정
    @Value("${gms.api.providers.gemini.base-url:https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta}")
    private String geminiBaseUrl;

    @Value("${gms.api.providers.gemini.endpoint:/models}")
    private String geminiEndpoint;

    // Claude Provider 설정
    @Value("${gms.api.providers.claude.base-url:https://gms.ssafy.io/gmsapi/api.anthropic.com/v1}")
    private String claudeBaseUrl;

    @Value("${gms.api.providers.claude.endpoint:/messages}")
    private String claudeEndpoint;

    /**
     * 서버 시작 시 도메인 가이드라인 로딩
     */
    @PostConstruct
    private void loadDomainGuidelines() {
        try {
            String jsonContent = guidelinesResource.getContentAsString(StandardCharsets.UTF_8);
            domainGuidelines = objectMapper.readValue(jsonContent, Map.class);
            log.info("도메인 가이드라인 로딩 완료 - version: {}",
                ((Map<String, Object>) domainGuidelines.get("metadata")).get("version"));
        } catch (IOException e) {
            log.warn("도메인 가이드라인 로딩 실패, 기본 프롬프트만 사용: {}", e.getMessage());
            domainGuidelines = Map.of();
        }
    }

    /**
     * LLM을 사용하여 환자 인사이트 생성
     */
    public LLMInsightResponse generateInsight(PatientInsightFeatures features) {
        log.info("LLM 인사이트 생성 요청 - patientId: {}, period: {} ~ {}",
            features.patientId(),
            features.period().currentStart(),
            features.period().currentEnd());

        try {
            // 시스템 프롬프트 + 유저 프롬프트 생성
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(features);

            // GMS API 호출
            Map<String, Object> requestBody = Map.of(
                "model", gmsModel,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.7,
                "max_tokens", 2000,
                "response_format", Map.of("type", "json_object")
            );

            String responseJson = callGmsApi(requestBody);

            // JSON 응답 파싱
            return parseLLMResponse(responseJson);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM 인사이트 생성 실패 - patientId: {}, error: {}",
                features.patientId(), e.getMessage(), e);
            throw new BusinessException(ErrorCode.INSIGHT_GENERATION_FAILED);
        }
    }

    /**
     * GMS API 호출 (Provider별 URL/헤더 자동 선택)
     */
    private String callGmsApi(Map<String, Object> requestBody) {
        // Provider별 URL 및 Endpoint 선택
        String baseUrl;
        String endpoint;

        switch (gmsProvider.toLowerCase()) {
            case "gemini" -> {
                baseUrl = geminiBaseUrl;
                endpoint = geminiEndpoint + "/" + gmsModel + ":generateContent?key=" + gmsApiKey;
            }
            case "claude" -> {
                baseUrl = claudeBaseUrl;
                endpoint = claudeEndpoint;
            }
            case "openai" -> {
                baseUrl = openaiBaseUrl;
                endpoint = openaiEndpoint;
            }
            default -> {
                log.warn("알 수 없는 provider: {}, openai로 대체", gmsProvider);
                baseUrl = openaiBaseUrl;
                endpoint = openaiEndpoint;
            }
        }

        // WebClient 빌드 (Provider별 헤더 설정)
        WebClient webClient = buildWebClient(baseUrl);

        try {
            String response = webClient.post()
                .uri(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    clientResponse -> {
                        log.error("GMS API 에러 - provider: {}, status: {}", gmsProvider, clientResponse.statusCode());
                        return Mono.error(new BusinessException(ErrorCode.LLM_API_ERROR));
                    }
                )
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();

            log.debug("GMS API 응답 수신 완료 - provider: {}, model: {}", gmsProvider, gmsModel);
            return response;

        } catch (Exception e) {
            log.error("GMS API 호출 실패 - provider: {}, error: {}", gmsProvider, e.getMessage(), e);
            throw new BusinessException(ErrorCode.LLM_API_ERROR);
        }
    }

    /**
     * Provider별로 적절한 WebClient 빌드
     */
    private WebClient buildWebClient(String baseUrl) {
        switch (gmsProvider.toLowerCase()) {
            case "gemini" -> {
                // Gemini는 쿼리스트링에 key 추가 (endpoint에서 이미 추가됨)
                return webClientBuilder
                    .baseUrl(baseUrl)
                    .defaultHeader("Content-Type", "application/json")
                    .build();
            }
            case "claude" -> {
                // Claude는 x-api-key 헤더 사용
                return webClientBuilder
                    .baseUrl(baseUrl)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("x-api-key", gmsApiKey)
                    .defaultHeader("anthropic-version", "2023-06-01")
                    .build();
            }
            case "openai" -> {
                // OpenAI는 Authorization: Bearer 사용
                return webClientBuilder
                    .baseUrl(baseUrl)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Authorization", "Bearer " + gmsApiKey)
                    .build();
            }
            default -> {
                // 기본값: OpenAI 형식
                return webClientBuilder
                    .baseUrl(baseUrl)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Authorization", "Bearer " + gmsApiKey)
                    .build();
            }
        }
    }

    /**
     * LLM 응답 파싱
     */
    private LLMInsightResponse parseLLMResponse(String responseJson) {
        try {
            // GMS API 응답 형식: {"choices": [{"message": {"content": "{...}"}}]}
            Map<String, Object> apiResponse = objectMapper.readValue(responseJson, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");

            if (choices == null || choices.isEmpty()) {
                log.error("GMS API 응답에 choices가 없습니다.");
                throw new BusinessException(ErrorCode.LLM_API_ERROR);
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");

            // content를 LLMInsightResponse로 파싱
            return objectMapper.readValue(content, LLMInsightResponse.class);

        } catch (JsonProcessingException e) {
            log.error("LLM 응답 파싱 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INSIGHT_GENERATION_FAILED);
        }
    }

    /**
     * 플래그별 도메인 가이드라인 추출 (RAG 방식)
     */
    private String extractRelevantGuidelines(InsightFlags flags) {
        if (domainGuidelines.isEmpty()) {
            return "";  // 가이드라인 로딩 실패 시 빈 문자열
        }

        StringBuilder guidelines = new StringBuilder();
        guidelines.append("\n\n**[참고: 임상 가이드라인 요약]**\n");
        guidelines.append("아래는 국내외 치매 진료 가이드라인(NICE NG97, PMC 논문, 한국 중앙치매센터)을 바탕으로 정리한 일반 원칙입니다.\n");
        guidelines.append("위험도 해석과 보호자 제안 시 이 원칙을 최대한 참고해주세요.\n\n");

        try {
            Map<String, Object> guidelinesMap = (Map<String, Object>) domainGuidelines.get("guidelines");

            // 공간 혼란
            if (flags.spatialConfusionDetected() && guidelinesMap.containsKey("spatial_confusion")) {
                Map<String, Object> spatial = (Map<String, Object>) guidelinesMap.get("spatial_confusion");
                guidelines.append("● **배회/길 잃음 (공간 혼란)**\n");
                guidelines.append("  - 임상적 근거: ").append(spatial.get("clinical_basis")).append("\n");
                List<String> recommendations = (List<String>) spatial.get("caregiver_recommendations");
                guidelines.append("  - 보호자 권장사항:\n");
                recommendations.stream().limit(3).forEach(r -> guidelines.append("    · ").append(r).append("\n"));
                guidelines.append("\n");
            }

            // 신체 상태 저하
            if (flags.physicalConditionDrop() && guidelinesMap.containsKey("physical_drop")) {
                Map<String, Object> physical = (Map<String, Object>) guidelinesMap.get("physical_drop");
                guidelines.append("● **신체 상태 저하 (수면/HRV/SpO2)**\n");

                // 수면
                if (physical.containsKey("sleep")) {
                    Map<String, Object> sleep = (Map<String, Object>) physical.get("sleep");
                    guidelines.append("  - 수면: ").append(sleep.get("clinical_impact")).append("\n");
                    List<String> sleepRec = (List<String>) sleep.get("recommendations");
                    sleepRec.stream().limit(2).forEach(r -> guidelines.append("    · ").append(r).append("\n"));
                }
                guidelines.append("\n");
            }

            // 불안/위험 증가
            if (flags.anxietyOrRiskEscalation() && guidelinesMap.containsKey("anxiety_escalation")) {
                Map<String, Object> anxiety = (Map<String, Object>) guidelinesMap.get("anxiety_escalation");
                guidelines.append("● **불안/위험 증가 (SOS/긴급통화)**\n");
                guidelines.append("  - 임상적 근거: ").append(anxiety.get("clinical_basis")).append("\n");
                List<String> recommendations = (List<String>) anxiety.get("caregiver_recommendations");
                recommendations.stream().limit(2).forEach(r -> guidelines.append("    · ").append(r).append("\n"));
                guidelines.append("\n");
            }

            // 패닉 반응
            if (flags.panicResponsePattern() && guidelinesMap.containsKey("panic_response")) {
                Map<String, Object> panic = (Map<String, Object>) guidelinesMap.get("panic_response");
                guidelines.append("● **패닉 반응 (반복 SOS/미응답)**\n");
                guidelines.append("  - ⚠️ 즉각 개입 필요: ").append(panic.get("clinical_basis")).append("\n");
                List<String> recommendations = (List<String>) panic.get("caregiver_recommendations");
                recommendations.stream().limit(2).forEach(r -> guidelines.append("    · ").append(r).append("\n"));
                guidelines.append("\n");
            }

            // 수면-활동 상관관계
            if (flags.sleepActivityCorrelation() && guidelinesMap.containsKey("sleep_activity_correlation")) {
                Map<String, Object> sleepActivity = (Map<String, Object>) guidelinesMap.get("sleep_activity_correlation");
                guidelines.append("● **수면-활동 상관관계**\n");
                guidelines.append("  - ").append(sleepActivity.get("clinical_basis")).append("\n");
                guidelines.append("\n");
            }

            // 일상 패턴 변화
            if (flags.routineChangeDetected() && guidelinesMap.containsKey("routine_change")) {
                Map<String, Object> routine = (Map<String, Object>) guidelinesMap.get("routine_change");
                guidelines.append("● **일상 패턴 변화**\n");
                guidelines.append("  - ").append(routine.get("clinical_basis")).append("\n");
                guidelines.append("\n");
            }

            // 일반 원칙 추가
            if (domainGuidelines.containsKey("general_principles")) {
                Map<String, Object> principles = (Map<String, Object>) domainGuidelines.get("general_principles");
                guidelines.append("**[일반 원칙]**\n");
                guidelines.append("- 비약물적 중재 우선: ").append(principles.get("non_pharmacological_first")).append("\n");
                guidelines.append("- 환경 조정: ").append(principles.get("environmental_modification")).append("\n");
                guidelines.append("- 조기 개입: ").append(principles.get("early_intervention")).append("\n");
            }

        } catch (Exception e) {
            log.warn("가이드라인 추출 중 오류 발생: {}", e.getMessage());
            return "";
        }

        return guidelines.toString();
    }

    /**
     * 시스템 프롬프트 생성
     */
    private String buildSystemPrompt() {
        return """
            당신은 치매 환자의 일상 활동과 건강 데이터를 분석하는 의료 AI 어시스턴트입니다.

            **역할:**
            - 보호자가 이해하기 쉽도록 환자의 상태를 요약하고 해석합니다.
            - 긍정적 신호와 경고 신호를 명확히 구분합니다.
            - 전문 용어 대신 일상적인 언어를 사용합니다.
            - 제공된 임상 가이드라인을 참고하여 근거 기반 제안을 합니다.

            **출력 형식 (JSON):**
            {
              "summary": "전체 요약 (2-3문장)",
              "overall_risk_level": "LOW | MEDIUM | HIGH",
              "positive_signals": ["긍정적 신호 1", "긍정적 신호 2"],
              "warning_signals": ["경고 신호 1", "경고 신호 2"],
              "possible_interpretations": ["해석 1", "해석 2"],
              "caregiver_suggestions": ["제안 1", "제안 2", "제안 3"],
              "data_notes": ["데이터 제약사항 1", "데이터 제약사항 2"]
            }

            **주의사항:**
            - 데이터가 부족한 경우, data_notes에 명시하고 가능한 범위에서 분석합니다.
            - 의학적 진단을 내리지 않으며, 관찰된 패턴만 설명합니다.
            - 보호자가 당장 실천 가능한 구체적이고 현실적인 제안을 합니다.
            - 임상 가이드라인에서 제시된 비약물적 중재를 우선적으로 권장합니다.
            - 반드시 JSON 형식으로만 응답합니다.
            """;
    }

    /**
     * 유저 프롬프트 생성 (RAG 방식으로 가이드라인 주입)
     */
    private String buildUserPrompt(PatientInsightFeatures features) {
        try {
            // PatientInsightFeatures를 JSON으로 변환
            String featuresJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(features);

            // 활성화된 플래그에 따라 관련 가이드라인 추출
            String relevantGuidelines = extractRelevantGuidelines(features.flags());

            return String.format("""
                다음은 환자의 %s 활동 및 건강 데이터입니다.

                **분석 기간:**
                - 현재 기간: %s ~ %s
                - 비교 기간: %s ~ %s

                **집계 데이터 (JSON):**
                %s

                **활성화된 위험 플래그:**
                - 일상 패턴 변화: %s (심각도: %d점)
                - 공간 혼란: %s (심각도: %d점)
                - 불안/위험 증가: %s (심각도: %d점)
                - 신체 상태 저하: %s (심각도: %d점)
                - 수면-활동 상관관계: %s (심각도: %d점)
                - 패닉 반응: %s (심각도: %d점)

                **전체 위험도 평가:** %s
                - 심각(7+점) 항목: %d개
                - 주의(4+점) 항목: %d개
                - 총 심각도 점수: %d점
                %s

                **요청사항:**
                1. 이전 기간 대비 현재 기간의 주요 변화를 분석해주세요.
                2. 위 임상 가이드라인을 참고하여 각 위험 신호를 해석해주세요.
                3. 보호자가 오늘부터 당장 실천할 수 있는 구체적인 행동을 3가지 이상 제안해주세요.
                4. 데이터가 부족한 부분은 data_notes에 명시해주세요.

                반드시 JSON 형식으로 응답해주세요.
                """,
                features.period().periodType().getDescription(),
                features.period().currentStart(),
                features.period().currentEnd(),
                features.period().previousStart(),
                features.period().previousEnd(),
                featuresJson,
                features.flags().routineChangeDetected() ? "예" : "아니오", features.flags().routineChangeSeverity(),
                features.flags().spatialConfusionDetected() ? "예" : "아니오", features.flags().spatialConfusionSeverity(),
                features.flags().anxietyOrRiskEscalation() ? "예" : "아니오", features.flags().anxietyEscalationSeverity(),
                features.flags().physicalConditionDrop() ? "예" : "아니오", features.flags().physicalDropSeverity(),
                features.flags().sleepActivityCorrelation() ? "예" : "아니오", features.flags().sleepActivitySeverity(),
                features.flags().panicResponsePattern() ? "예" : "아니오", features.flags().panicResponseSeverity(),
                features.flags().estimateRiskLevel(),
                features.flags().severeCount(),
                features.flags().moderateCount(),
                features.flags().totalSeverity(),
                relevantGuidelines
            );

        } catch (JsonProcessingException e) {
            log.error("유저 프롬프트 생성 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INSIGHT_GENERATION_FAILED);
        }
    }
}
