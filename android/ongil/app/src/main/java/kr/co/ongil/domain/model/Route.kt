package kr.co.ongil.domain.model

/**
 * 길찾기 경로 정보를 나타내는 데이터 클래스 (도메인 모델)
 * UI 계층에서 직접 사용됩니다.
 *
 * @param totalTimeMinutes 총 소요 시간 (분)
 * @param totalDistanceMeters 총 거리 (미터)
 * @param path 경로를 구성하는 위도/경도 좌표 리스트
 * @param navigationId 길찾기 ID (종료 시 필요, 선택적)
 * @param startLocationName 출발지 이름 (선택적)
 * @param endLocationName 도착지 이름 (선택적)
 */
data class Route(
    val totalTimeMinutes: Int,
    val totalDistanceMeters: Int,
    val path: List<LatLng>,
    val navigationId: String? = null,
    val startLocationName: String? = null,
    val endLocationName: String? = null
)
