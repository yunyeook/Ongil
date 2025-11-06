package kr.co.ongil.domain.map.dto.tmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * TMAP 보행자 경로 탐색 API 응답 DTO
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmapPedestrianRouteResponse(
    String type,
    List<Feature> features
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Feature(
        String type,
        Geometry geometry,
        Properties properties
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(
        String type,  // "Point" 또는 "LineString"
        Object coordinates  // Point: [경도, 위도], LineString: [[경도, 위도], ...]
    ) {
        /**
         * coordinates를 List<List<Double>> 형태로 반환
         * Point인 경우 [[경도, 위도]]로 변환
         * LineString인 경우 그대로 반환
         */
        @SuppressWarnings("unchecked")
        public List<List<Double>> getCoordinatesAsListOfList() {
            if (coordinates instanceof List<?> list) {
                if (!list.isEmpty() && list.get(0) instanceof List) {
                    // LineString: [[경도, 위도], ...]
                    return (List<List<Double>>) coordinates;
                } else {
                    // Point: [경도, 위도] -> [[경도, 위도]]로 변환
                    return List.of((List<Double>) coordinates);
                }
            }
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Properties(
        Integer totalDistance,  // 총 거리(m) - SP에만 존재
        Integer totalTime,      // 총 시간(초) - SP에만 존재
        Integer index,          // 경로 순번
        Integer pointIndex,     // 안내점 순번
        Integer lineIndex,      // 구간 순번
        String name,            // 도로명/안내점명
        String description,     // 안내 메시지
        String direction,       // 방면
        String intersectionName,
        String nearPoiName,
        String nearPoiX,
        String nearPoiY,
        Integer turnType,       // 회전정보: 11=직진, 12=좌회전, 13=우회전, 14=유턴, 125=육교, 126=지하보도, 127=계단, 211=횡단보도, 200=출발지, 201=도착지
        String pointType,       // SP=출발지, EP=도착지, GP=안내점, PP=경유지
        String facilityType,    // 시설물타입: 11=일반보행자도로, 12=육교, 14=지하보도, 15=횡단보도
        String facilityName,
        Integer distance,       // 구간 거리(m)
        Integer time,          // 구간 시간(초)
        Integer roadType,      // 21~24: 보행자도로 타입
        Integer categoryRoadType,
        String roadName,
        String guidePointName,
        String crossName
    ) {}
}