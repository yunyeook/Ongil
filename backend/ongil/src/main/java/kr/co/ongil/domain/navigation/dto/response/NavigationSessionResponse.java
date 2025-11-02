package kr.co.ongil.domain.navigation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.map.dto.response.RouteResponse;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "길안내 세션 응답")
public class NavigationSessionResponse {

    @Schema(description = "네비게이션 ID", example = "nav_123")
    private String navigationId;

    @Schema(description = "경로 정보")
    private RouteResponse route;

    @Schema(description = "시작 시간")
    private LocalDateTime startedAt;

    @Schema(description = "예상 도착 시간")
    private LocalDateTime expectedArrival;

    @Schema(description = "시작 주체", example = "PATIENT")
    private String initiatedBy;


    /**
     * 길안내 세션 응답 생성
     */
    public static NavigationSessionResponse of(
        String navigationId,
        RouteResponse route,
        LocalDateTime startedAt,
        LocalDateTime expectedArrival,
        String initiatedBy
    ) {
        return NavigationSessionResponse.builder()
            .navigationId(navigationId)
            .route(route)
            .startedAt(startedAt)
            .expectedArrival(expectedArrival)
            .initiatedBy(initiatedBy)
            .build();
    }
}