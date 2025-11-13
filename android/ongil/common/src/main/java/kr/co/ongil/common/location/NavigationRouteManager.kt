package kr.co.ongil.common.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 길찾기 경로 정보를 관리하는 Singleton 클래스
 * 경로 이탈 모니터링을 위해 현재 경로 정보를 저장
 */
@Singleton
class NavigationRouteManager @Inject constructor() {

    data class NavigationRoute(
        val navigationId: String,
        val path: List<LatLng>
    )

    data class LatLng(
        val latitude: Double,
        val longitude: Double
    )

    private val _currentRoute = MutableStateFlow<NavigationRoute?>(null)
    val currentRoute: StateFlow<NavigationRoute?> = _currentRoute.asStateFlow()

    /**
     * 경로 설정
     */
    fun setRoute(navigationId: String, path: List<LatLng>) {
        _currentRoute.value = NavigationRoute(navigationId, path)
    }

    /**
     * 경로 초기화
     */
    fun clearRoute() {
        _currentRoute.value = null
    }

    /**
     * 현재 경로 가져오기
     */
    fun getCurrentRoute(): NavigationRoute? = _currentRoute.value
}