package kr.co.ongil.domain.call.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 통화 녹음 메타데이터 생성 Request
 */
@Schema(description = "통화 녹음 메타데이터 생성")
public record CreateCallRecordingRequest(

    @Schema(description = "통화 로그 ID", example = "1")
    @NotNull(message = "통화 로그 ID는 필수입니다.")
    Integer callLogId,

    @Schema(description = "로컬 파일 경로", example = "/storage/recordings/call_20250115_103000.m4a")
    @NotBlank(message = "파일 경로는 필수입니다.")
    String filePath,

    @Schema(description = "파일 크기 (바이트)", example = "1048576")
    Long fileSize,

    @Schema(description = "녹음 길이 (초)", example = "295")
    Integer duration
) {
}
