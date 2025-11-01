package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import kr.co.ongil.domain.map.dto.tmap.TmapPedestrianRouteResponse;

@Schema(description = "경로 응답")
public record RouteResponse(
    @Schema(description = "출발지 정보")
    LocationInfo startLocation,

    @Schema(description = "도착지 정보")
    LocationInfo endLocation,

    @Schema(description = "총 거리(미터)", example = "1234")
    Integer totalDistance,

    @Schema(description = "총 소요 시간(초)", example = "600")
    Integer totalTime,

    @Schema(description = "경로 좌표 목록")
    List<CoordinateResponse> path,

    @Schema(description = "턴바이턴 안내 목록")
    List<RouteGuide> guides
) {
    @Schema(description = "위치 정보")
    public record LocationInfo(
        @Schema(description = "위도", example = "37.5665")
        Double latitude,

        @Schema(description = "경도", example = "126.9780")
        Double longitude,

        @Schema(description = "장소명", example = "서울역")
        String name
    ) {
        public static LocationInfo of(Double latitude, Double longitude, String name) {
            return new LocationInfo(latitude, longitude, name);
        }
    }

    @Schema(description = "경로 안내")
    public record RouteGuide(
        @Schema(description = "안내점 순번 (path 배열의 인덱스)", example = "1")
        Integer index,

        @Schema(description = "안내 메시지", example = "양화로를 따라 100m 직진")
        String instruction,

        @Schema(description = "구간 거리(미터)", example = "100")
        Integer distance,

        @Schema(description = "구간 소요 시간(초)", example = "30")
        Integer time,

        @Schema(description = "회전 타입 코드 (TMAP 표준)", example = "12")
        Integer turnTypeCode,

        @Schema(description = "회전 방향", example = "직진")
        String turnType,

        @Schema(description = "도로명", example = "양화로")
        String roadName
    ) {
        public static RouteGuide of(Integer index, String instruction, Integer distance,
            Integer time, Integer turnTypeCode, String turnType, String roadName) {
            return new RouteGuide(index, instruction, distance, time, turnTypeCode, turnType, roadName);
        }
    }

    /**
     * TMAP 응답을 RouteResponse로 변환
     */
    public static RouteResponse parseRouteResponse(
        TmapPedestrianRouteResponse tmapResponse,
        Double startLat, Double startLon,
        Double endLat, Double endLon,
        String startName, String endName
    ) {
        List<CoordinateResponse> pathCoordinates = new ArrayList<>();
        List<RouteGuide> guides = new ArrayList<>();
        Integer totalDistance = 0;
        Integer totalTime = 0;

        // 임시로 저장할 변수들
        Integer currentDistance = 0;
        Integer currentTime = 0;
        String currentRoadName = "";

        for (TmapPedestrianRouteResponse.Feature feature : tmapResponse.features()) {
            TmapPedestrianRouteResponse.Properties props = feature.properties();

            // totalDistance와 totalTime은 SP(출발지)에서만 추출
            if ("SP".equals(props.pointType())) {
                totalDistance = props.totalDistance() != null ? props.totalDistance() : 0;
                totalTime = props.totalTime() != null ? props.totalTime() : 0;
            }

            // LineString: 경로 좌표들 + 구간 정보 추출
            if ("LineString".equals(feature.geometry().type())) {
                @SuppressWarnings("unchecked")
                List<List<Double>> coordinates = (List<List<Double>>) feature.geometry().coordinates();

                for (List<Double> coord : coordinates) {
                    if (coord.size() >= 2) {
                        pathCoordinates.add(CoordinateResponse.of(
                            coord.get(1),  // 위도
                            coord.get(0)   // 경도
                        ));
                    }
                }

                // LineString에서 distance, time, roadName 저장
                currentDistance = props.distance();
                currentTime = props.time();
                currentRoadName = props.name() != null ? props.name() : "";
            }

            // Point: 안내점 정보 추출 (SP 출발지 제외)
            if ("Point".equals(feature.geometry().type())
                && props.description() != null
                && !"SP".equals(props.pointType())) {

                guides.add(RouteGuide.of(
                    props.pointIndex(),
                    props.description(),
                    currentDistance,
                    currentTime,
                    props.turnType(),
                    getTurnTypeDescription(props.turnType()),
                    currentRoadName
                ));
            }
        }

        return new RouteResponse(
            LocationInfo.of(startLat, startLon, startName),
            LocationInfo.of(endLat, endLon, endName),
            totalDistance,
            totalTime,
            pathCoordinates,
            guides
        );
    }

    /**
     * turnType 코드를 한글 설명으로 변환
     */
    private static String getTurnTypeDescription(Integer turnType) {
        if (turnType == null) return "직진";

        return switch (turnType) {
            case 11 -> "직진";
            case 12 -> "좌회전";
            case 13 -> "우회전";
            case 14 -> "유턴";
            case 16 -> "8시 방향 좌회전";
            case 17 -> "10시 방향 좌회전";
            case 18 -> "2시 방향 우회전";
            case 19 -> "4시 방향 우회전";
            case 125 -> "육교";
            case 126 -> "지하보도";
            case 127 -> "계단";
            case 128 -> "경사로";
            case 129 -> "계단+경사로";
            case 211 -> "횡단보도";
            case 212 -> "좌측 횡단보도";
            case 213 -> "우측 횡단보도";
            case 214 -> "8시 방향 횡단보도";
            case 215 -> "10시 방향 횡단보도";
            case 216 -> "2시 방향 횡단보도";
            case 217 -> "4시 방향 횡단보도";
            case 218 -> "엘리베이터";
            case 200 -> "출발지";
            case 201 -> "도착지";
            default -> "직진";
        };
    }
}