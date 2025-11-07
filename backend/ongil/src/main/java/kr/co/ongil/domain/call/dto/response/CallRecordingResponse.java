package kr.co.ongil.domain.call.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.call.entity.CallRecording;

import java.time.LocalDateTime;

/**
 * 통화 녹음 메타데이터 응답
 */
@Schema(description = "통화 녹음 메타데이터 정보")
public record CallRecordingResponse(

    @Schema(description = "녹음 ID", example = "1")
    Integer id,

    @Schema(description = "통화 로그 ID", example = "1")
    Integer callLogId,

    @Schema(description = "로컬 파일 경로", example = "/storage/recordings/call_20250115_103000.m4a")
    String filePath,

    @Schema(description = "파일 크기 (바이트)", example = "1048576")
    Long fileSize,

    @Schema(description = "녹음 길이 (초)", example = "295")
    Integer duration,

    @Schema(description = "생성 시간", example = "2025-01-15T10:36:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime createdAt
) {

    /**
     * Entity -> Response 변환
     */
    public static CallRecordingResponse from(CallRecording recording) {
        return new CallRecordingResponse(
            recording.getId(),
            recording.getCallLog().getId(),
            recording.getFilePath(),
            recording.getFileSize(),
            recording.getDuration(),
            recording.getCreatedAt()
        );
    }
}
