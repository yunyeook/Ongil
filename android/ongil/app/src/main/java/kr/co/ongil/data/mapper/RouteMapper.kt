package kr.co.ongil.data.mapper

import kr.co.ongil.data.model.map.RouteDto
import kr.co.ongil.domain.model.LatLng
import kr.co.ongil.domain.model.Route

/**
 * RouteDto를 도메인 모델 Route로 변환
 */
fun RouteDto.toRoute(): Route {
    return Route(
        totalTimeMinutes = this.totalTime / 60, // 초를 분으로 변환
        totalDistanceMeters = this.totalDistance,
        path = this.path.map { pathDto ->
            LatLng(
                latitude = pathDto.latitude,
                longitude = pathDto.longitude
            )
        }
    )
}
