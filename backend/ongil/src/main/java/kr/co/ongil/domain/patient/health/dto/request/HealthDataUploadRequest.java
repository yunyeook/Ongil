package kr.co.ongil.domain.patient.health.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 건강 데이터 업로드 요청
 * 여러 개의 건강 데이터를 한 번에 업로드
 */
@Schema(description = "건강 데이터 업로드 요청")
public record HealthDataUploadRequest(

    @Schema(description = "생체 데이터 리스트", required = true)
    @NotEmpty(message = "업로드할 데이터가 최소 1개 이상 필요합니다.")
    @Valid
    List<HealthDataRecordRequest> records
) {
}
