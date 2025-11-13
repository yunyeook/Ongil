package kr.co.ongil.global.sse.event;

import kr.co.ongil.domain.map.dto.response.CoordinateInfo;
import kr.co.ongil.domain.map.dto.response.RouteResponse;

public record LocationUpdatedEvent(
    Integer patientId,
    CoordinateInfo coordinate
) {
    public static LocationUpdatedEvent of(
        Integer patientId,
        CoordinateInfo coordinate
    ) {
        return new LocationUpdatedEvent(patientId,coordinate);
    }


}