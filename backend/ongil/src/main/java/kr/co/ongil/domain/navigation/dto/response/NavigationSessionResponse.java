package kr.co.ongil.domain.navigation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.domain.map.dto.response.RouteResponse;

import java.time.LocalDateTime;

@Schema(name="NavigationSessionResponse",description = "길안내 세션 응답")
public record NavigationSessionResponse (

    @Schema(description = "네비게이션 ID", example = "nav_123")
    String navigationId,

    @Schema(description = "경로 정보")
    RouteResponse route,

    @Schema(description = "시작 시간")
    LocalDateTime startedAt,

    @Schema(description = "예상 도착 시간")
    LocalDateTime expectedArrival,

    @Schema(description = "시작 주체", example = "PATIENT")
    String initiatedBy){


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
        return new NavigationSessionResponse(
            navigationId, route, startedAt, expectedArrival, initiatedBy
        );
    }
}