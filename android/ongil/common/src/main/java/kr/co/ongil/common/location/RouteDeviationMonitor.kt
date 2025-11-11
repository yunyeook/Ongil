package kr.co.ongil.common.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 길찾기 경로 이탈 모니터
 * 현재 위치가 경로로부터 50m 이상 벗어나면 이상상황으로 판정
 */
class RouteDeviationMonitor(
    private val routePath: List<NavigationRouteManager.LatLng>,
    private val deviationThresholdMeters: Double = DEFAULT_DEVIATION_THRESHOLD,
    private val onRouteDeviation: (distanceFromRoute: Double) -> Unit
) {
    companion object {
        // 기본 경로 이탈 판정 거리 (미터)
        const val DEFAULT_DEVIATION_THRESHOLD = 50.0

        /**
         * 두 지점 간 거리 계산 (Haversine formula, 미터 단위)
         */
        fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val earthRadius = 6371000.0 // 지구 반경 (미터)
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)

            val a = sin(dLat / 2).pow(2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2)

            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadius * c
        }

        /**
         * 점에서 선분까지의 최단 거리 계산
         *
         * @param px 점의 x 좌표 (경도)
         * @param py 점의 y 좌표 (위도)
         * @param x1 선분 시작점 x (경도)
         * @param y1 선분 시작점 y (위도)
         * @param x2 선분 끝점 x (경도)
         * @param y2 선분 끝점 y (위도)
         * @return 최단 거리 (미터)
         */
        private fun pointToSegmentDistance(
            px: Double, py: Double,
            x1: Double, y1: Double,
            x2: Double, y2: Double
        ): Double {
            // 선분의 길이 제곱
            val segmentLengthSquared = (x2 - x1).pow(2) + (y2 - y1).pow(2)

            if (segmentLengthSquared == 0.0) {
                // 선분이 점인 경우
                return calculateDistance(py, px, y1, x1)
            }

            // 선분 위의 가장 가까운 점을 찾기 위한 매개변수 t (0 ~ 1)
            val t = ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / segmentLengthSquared
            val tClamped = t.coerceIn(0.0, 1.0)

            // 선분 위의 가장 가까운 점
            val closestX = x1 + tClamped * (x2 - x1)
            val closestY = y1 + tClamped * (y2 - y1)

            // 거리 계산
            return calculateDistance(py, px, closestY, closestX)
        }

        /**
         * 현재 위치에서 경로까지의 최단 거리 계산
         *
         * @param currentLat 현재 위도
         * @param currentLon 현재 경도
         * @param path 경로 포인트 리스트
         * @return 경로까지의 최단 거리 (미터)
         */
        fun calculateDistanceToPath(
            currentLat: Double,
            currentLon: Double,
            path: List<NavigationRouteManager.LatLng>
        ): Double {
            if (path.isEmpty()) return Double.MAX_VALUE
            if (path.size == 1) {
                return calculateDistance(currentLat, currentLon, path[0].latitude, path[0].longitude)
            }

            var minDistance = Double.MAX_VALUE

            // 경로의 각 선분에 대해 최단 거리 계산
            for (i in 0 until path.size - 1) {
                val p1 = path[i]
                val p2 = path[i + 1]

                val distance = pointToSegmentDistance(
                    currentLon, currentLat,
                    p1.longitude, p1.latitude,
                    p2.longitude, p2.latitude
                )

                if (distance < minDistance) {
                    minDistance = distance
                }
            }

            return minDistance
        }
    }

    // 경로 이탈 알림 여부 (한 번만 알림)
    private var deviationAlerted = false

    /**
     * 위치 업데이트 처리
     */
    fun updateLocation(latitude: Double, longitude: Double) {
        if (routePath.isEmpty()) return

        // 현재 위치에서 경로까지의 최단 거리 계산
        val distanceFromRoute = calculateDistanceToPath(latitude, longitude, routePath)

        // 경로 이탈 판정
        if (distanceFromRoute > deviationThresholdMeters) {
            if (!deviationAlerted) {
                deviationAlerted = true
                onRouteDeviation(distanceFromRoute)
            }
        } else {
            // 경로로 복귀하면 알림 상태 리셋
            deviationAlerted = false
        }
    }

    /**
     * 모니터링 리셋
     */
    fun reset() {
        deviationAlerted = false
    }

    /**
     * 현재 경로 이탈 여부
     */
    fun isDeviated(): Boolean = deviationAlerted
}