package kr.co.ongil.global.sse.event;
import kr.co.ongil.domain.map.dto.response.CoordinateInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationUpdatedEvent {
    private final Integer patientId;
    private final CoordinateInfo coordinate;
}
