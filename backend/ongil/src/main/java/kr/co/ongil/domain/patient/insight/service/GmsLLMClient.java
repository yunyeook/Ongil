package kr.co.ongil.domain.patient.insight.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.ongil.domain.patient.insight.dto.internal.LLMInsightResponse;
import kr.co.ongil.domain.patient.insight.dto.internal.PatientInsightFeatures;
import kr.co.ongil.global.exception.BusinessException;
import kr.co.ongil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
     * 시스템 프롬프트 생성
     */
    private String buildSystemPrompt() {
        return """
            당신은 치매 환자의 일상 활동과 건강 데이터를 분석하는 의료 AI 어시스턴트입니다.

            **역할:**
            - 보호자가 이해하기 쉽도록 환자의 상태를 요약하고 해석합니다.
            - 긍정적 신호와 경고 신호를 명확히 구분합니다.
            - 전문 용어 대신 일상적인 언어를 사용합니다.

            **출력 형식 (JSON):**
            {
              "summary": "전체 요약 (2-3문장)",
              "overall_risk_level": "LOW | MEDIUM | HIGH",
              "positive_signals": ["긍정적 신호 1", "긍정적 신호 2"],
              "warning_signals": ["경고 신호 1", "경고 신호 2"],
              "possible_interpretations": ["해석 1", "해석 2"],
              "caregiver_suggestions": ["제안 1", "제안 2"],
              "data_notes": ["데이터 제약사항 1", "데이터 제약사항 2"]
            }

            **주의사항:**
            - 데이터가 부족한 경우, data_notes에 명시하고 가능한 범위에서 분석합니다.
            - 의학적 진단을 내리지 않으며, 관찰된 패턴만 설명합니다.
            - 보호자에게 실질적인 도움이 되는 구체적인 제안을 합니다.
            - 반드시 JSON 형식으로만 응답합니다.
            """;
    }

    /**
     * 유저 프롬프트 생성
     */
    private String buildUserPrompt(PatientInsightFeatures features) {
        try {
            // PatientInsightFeatures를 JSON으로 변환
            String featuresJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(features);

            return String.format("""
                다음은 환자의 %s 활동 및 건강 데이터입니다.

                **분석 기간:**
                - 현재 기간: %s ~ %s
                - 비교 기간: %s ~ %s

                **데이터:**
                %s

                **요청사항:**
                1. 이전 기간 대비 현재 기간의 변화를 분석해주세요.
                2. 특히 다음 플래그가 활성화되었습니다:
                   - 일상 패턴 변화: %s
                   - 공간 혼란: %s
                   - 불안/위험 증가: %s
                   - 신체 상태 저하: %s
                   - 수면-활동 상관관계: %s
                   - 패닉 반응: %s

                3. 보호자가 주의해야 할 점과 실천 가능한 제안을 해주세요.
                4. 데이터가 부족한 부분이 있다면 data_notes에 명시해주세요.

                반드시 JSON 형식으로 응답해주세요.
                """,
                features.period().periodType().getDescription(),
                features.period().currentStart(),
                features.period().currentEnd(),
                features.period().previousStart(),
                features.period().previousEnd(),
                featuresJson,
                features.flags().routineChangeDetected() ? "예" : "아니오",
                features.flags().spatialConfusionDetected() ? "예" : "아니오",
                features.flags().anxietyOrRiskEscalation() ? "예" : "아니오",
                features.flags().physicalConditionDrop() ? "예" : "아니오",
                features.flags().sleepActivityCorrelation() ? "예" : "아니오",
                features.flags().panicResponsePattern() ? "예" : "아니오"
            );

        } catch (JsonProcessingException e) {
            log.error("유저 프롬프트 생성 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.INSIGHT_GENERATION_FAILED);
        }
    }
}
