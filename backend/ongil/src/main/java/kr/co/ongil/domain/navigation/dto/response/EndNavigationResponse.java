package kr.co.ongil.domain.navigation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "길안내 종료 응답")
public class EndNavigationResponse {

    @Schema(description = "네비게이션 ID", example = "1")
    private String navigationId;

    @Schema(description = "시작 시간")
    private LocalDateTime startedAt;

    @Schema(description = "종료 시간")
    private LocalDateTime endedAt;

    @Schema(description = "정상 종료 여부")
    private Boolean isSuccessful;

    @Schema(description = "소요 시간 (초)", example = "900")
    private Long durationSeconds;

    public static EndNavigationResponse of(
        String navigationId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Boolean isSuccessful
    ) {
        long durationSeconds = java.time.Duration.between(startedAt, endedAt).getSeconds();

        return EndNavigationResponse.builder()
            .navigationId(navigationId)
            .startedAt(startedAt)
            .endedAt(endedAt)
            .isSuccessful(isSuccessful)
            .durationSeconds(durationSeconds)
            .build();
    }
}