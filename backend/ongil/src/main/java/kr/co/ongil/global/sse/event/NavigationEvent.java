package kr.co.ongil.global.sse.event;
import kr.co.ongil.domain.map.dto.response.RouteResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Int;


public record NavigationEvent (
    Integer patientId,
    Integer initiatorId, // 이벤트 발신자 ID
    String userType, // "PATIENT" or "GUARDIAN"
    RouteResponse route,
    String status // "STARTED" or "ENDED"
){

    public static NavigationEvent of(
        Integer patientId,
        Integer initiatorId,
        String initiatorRole,
        RouteResponse route,
        String status
    ) {
        return new NavigationEvent(patientId, initiatorId, initiatorRole, route, status);
    }
}
