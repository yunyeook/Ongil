package kr.co.ongil.domain.patient.health.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.patient.health.entity.HealthDataType;

import java.util.List;

/**
 * 건강 데이터 목록 응답
 */
@Schema(description = "건강 데이터 목록 응답")
public record HealthDataListResponse(

    @Schema(description = "환자 ID", example = "2")
    Integer patientId,

    @Schema(description = "데이터 종류 (null이면 전체)", example = "HEART_RATE")
    HealthDataType type,

    @Schema(description = "건강 데이터 레코드 목록")
    List<HealthDataRecordResponse> records
) {

    /**
     * 정적 팩토리 메서드
     */
    public static HealthDataListResponse of(Integer patientId, HealthDataType type, List<HealthDataRecordResponse> records) {
        return new HealthDataListResponse(patientId, type, records);
    }
}
