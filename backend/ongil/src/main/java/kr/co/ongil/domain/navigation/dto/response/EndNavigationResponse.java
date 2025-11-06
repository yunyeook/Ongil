package kr.co.ongil.domain.navigation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name="EndNavigationResponse",description = "길안내 종료 응답")
public record EndNavigationResponse (

    @Schema(description = "네비게이션 ID", example = "1")
     String navigationId,

    @Schema(description = "시작 시간")
    LocalDateTime startedAt,

    @Schema(description = "종료 시간")
    LocalDateTime endedAt,

    @Schema(description = "정상 종료 여부")
    Boolean isSuccessful,

    @Schema(description = "소요 시간 (초)", example = "900")
    Long durationSeconds){

    public static EndNavigationResponse of(
        String navigationId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Boolean isSuccessful
    ) {
        long durationSeconds = java.time.Duration.between(startedAt, endedAt).getSeconds();

        return new EndNavigationResponse(
            navigationId, startedAt, endedAt, isSuccessful, durationSeconds
        );
    }
}