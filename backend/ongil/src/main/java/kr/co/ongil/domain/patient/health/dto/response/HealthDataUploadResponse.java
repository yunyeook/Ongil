package kr.co.ongil.domain.patient.health.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 건강 데이터 업로드 응답
 */
@Schema(description = "건강 데이터 업로드 응답")
public record HealthDataUploadResponse(

    @Schema(description = "저장된 데이터 개수", example = "4")
    Integer uploadedCount
) {

    /**
     * 정적 팩토리 메서드
     */
    public static HealthDataUploadResponse from(Integer count) {
        return new HealthDataUploadResponse(count);
    }
}
