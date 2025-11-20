package kr.co.ongil.wear.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kr.co.ongil.common.location.LocationPoint
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.common.location.NavigationRouteManager
import kr.co.ongil.common.location.RouteDeviationMonitor
import kr.co.ongil.wear.data.datasource.sync.PhoneDataSyncManager
import kr.co.ongil.wear.domain.repository.LocationRepository
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Wear OS 네비게이션 ViewModel (블루투스 모델)
 *
 * 주요 기능:
 * - 화살표 기반 경로 안내
 * - 경로 이탈 감지 및 알림
 * - Common 모듈 통합 (NavigationRouteManager, RouteDeviationMonitor)
 * - 실시간 위치 기반 방향 계산
 */
@HiltViewModel
class NavigationViewModel @Inject constructor(
    private val navigationRouteManager: NavigationRouteManager,
    private val locationStreamBus: LocationStreamBus,
    private val locationRepository: LocationRepository,
    private val phoneDataSyncManager: PhoneDataSyncManager
) : ViewModel() {

    companion object {
        private const val TAG = "NavigationViewModel"
    }

    // === UI 상태 관리 ===

    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()

    private var routeDeviationMonitor: RouteDeviationMonitor? = null

    // === 초기화 ===

    init {
        observeLocation()
        observeRoute()
        setupPhoneDataSync()
    }

    // === 위치 관찰 ===

    /**
     * 실시간 위치 업데이트 관찰
     */
    private fun observeLocation() {
        viewModelScope.launch {
            locationStreamBus.updates.collect { location ->
                updateCurrentLocation(location)
            }
        }
    }

    /**
     * 현재 위치 업데이트 및 방향 계산
     */
    private fun updateCurrentLocation(location: LocationPoint) {
        _uiState.update { state ->
            state.copy(
                currentLatitude = location.latitude,
                currentLongitude = location.longitude,
                currentBearing = location.bearing
            )
        }

        // 경로 이탈 감지
        routeDeviationMonitor?.updateLocation(location.latitude, location.longitude)

        // 다음 waypoint까지 방향 및 거리 계산
        calculateNavigationDirection(location)
    }

    // === 경로 관찰 ===

    /**
     * 현재 경로 변경 관찰
     */
    private fun observeRoute() {
        viewModelScope.launch {
            navigationRouteManager.currentRoute.collect { route ->
                if (route != null) {
                    setupRoute(route)
                } else {
                    clearRoute()
                }
            }
        }
    }

    /**
     * Phone 데이터 동기화 설정
     *
     * Phone에서 경로 데이터 받으면 자동으로 네비게이션 시작
     */
    private fun setupPhoneDataSync() {
        // 네비게이션 경로 수신 리스너 설정
        phoneDataSyncManager.setOnNavigationRouteReceivedListener { navigationData ->
            viewModelScope.launch {
                try {
                    // JSON 경로 파싱
                    val path = parseRoutePath(navigationData.routePath)

                    // 네비게이션 시작
                    startNavigation(
                        navigationId = navigationData.navigationId,
                        path = path,
                        startLocationName = navigationData.startLocationName,
                        endLocationName = navigationData.endLocationName,
                        totalTimeMinutes = navigationData.totalTimeMinutes,
                        totalDistanceMeters = navigationData.totalDistanceMeters
                    )

                    Log.d(TAG, "✓ 경로 데이터 수신 및 네비게이션 시작: ${navigationData.navigationId}")
                } catch (e: Exception) {
                    Log.e(TAG, "경로 데이터 처리 실패", e)
                }
            }
        }
    }

    /**
     * JSON 경로 문자열 파싱
     *
     * @param routePathJson JSON 형식: [{"lat":37.5,"lon":127.0},...]
     * @return List<NavigationRouteManager.LatLng>
     */
    private fun parseRoutePath(routePathJson: String): List<NavigationRouteManager.LatLng> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val coords = json.decodeFromString<List<RouteCoordinate>>(routePathJson)
            coords.map { NavigationRouteManager.LatLng(it.lat, it.lon) }
        } catch (e: Exception) {
            Log.e(TAG, "경로 파싱 실패: $routePathJson", e)
            emptyList()
        }
    }

    // === 경로 설정 ===

    /**
     * 경로 시작 (Phone으로부터 경로 데이터 수신)
     *
     * @param navigationId 네비게이션 ID
     * @param path 경로 좌표 리스트
     * @param startLocationName 출발지 이름
     * @param endLocationName 목적지 이름
     * @param totalTimeMinutes 예상 소요 시간 (분)
     * @param totalDistanceMeters 총 거리 (미터)
     */
    fun startNavigation(
        navigationId: String,
        path: List<NavigationRouteManager.LatLng>,
        startLocationName: String,
        endLocationName: String,
        totalTimeMinutes: Int,
        totalDistanceMeters: Int
    ) {
        Log.d(TAG, "=== [WATCH] startNavigation: navigationId=$navigationId")

        // NavigationRouteManager에 경로 설정
        navigationRouteManager.setRoute(
            navigationId = navigationId,
            path = path,
            startLocationName = startLocationName,
            endLocationName = endLocationName,
            totalTimeMinutes = totalTimeMinutes,
            totalDistanceMeters = totalDistanceMeters
        )

        // LocationRepository를 통해 Phone으로 네비게이션 시작 알림
        viewModelScope.launch {
            try {
                val startLocation = kr.co.ongil.wear.domain.model.NavigationLocation(
                    name = startLocationName,
                    latitude = path.firstOrNull()?.latitude ?: 0.0,
                    longitude = path.firstOrNull()?.longitude ?: 0.0,
                    address = startLocationName
                )
                val endLocation = kr.co.ongil.wear.domain.model.NavigationLocation(
                    name = endLocationName,
                    latitude = path.lastOrNull()?.latitude ?: 0.0,
                    longitude = path.lastOrNull()?.longitude ?: 0.0,
                    address = endLocationName
                )

                locationRepository.startNavigation(
                    patientId = 0L, // Phone에서 처리
                    startLocation = startLocation,
                    endLocation = endLocation,
                    initiatedBy = "PATIENT"
                )
                Log.d(TAG, "✓ Navigation start request sent to Phone")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send navigation start request", e)
            }
        }
    }

    /**
     * 경로 설정 (NavigationRouteManager로부터 수신)
     */
    private fun setupRoute(route: NavigationRouteManager.NavigationRoute) {
        Log.d(TAG, "Setting up route: ${route.endLocationName}, ${route.path.size} points")

        // UI 상태 업데이트
        _uiState.update {
            it.copy(
                isNavigating = true,
                destinationName = route.endLocationName,
                totalDistanceMeters = route.totalDistanceMeters,
                estimatedTimeMinutes = route.totalTimeMinutes,
                routePath = route.path,
                currentWaypointIndex = 0,
                isRouteDeviated = false
            )
        }

        // 경로 이탈 모니터 설정
        setupDeviationMonitor(route.path)
    }

    /**
     * 경로 이탈 모니터 설정
     */
    private fun setupDeviationMonitor(path: List<NavigationRouteManager.LatLng>) {
        routeDeviationMonitor = RouteDeviationMonitor(
            routePath = path,
            deviationThresholdMeters = 50.0,
            emergencyTimeoutMillis = 300000L, // 5분
            onRouteDeviation = { distanceFromRoute ->
                Log.w(TAG, "경로 이탈 감지: ${distanceFromRoute.toInt()}m")
                _uiState.update {
                    it.copy(
                        isRouteDeviated = true,
                        deviationDistanceMeters = distanceFromRoute.toInt()
                    )
                }
            },
            onEmergencyTimeout = {
                Log.e(TAG, "응급상황: 5분 이상 경로 이탈")
                _uiState.update {
                    it.copy(isEmergency = true)
                }
                // TODO: SOS 알림 전송
            }
        )
    }

    // === 방향 계산 ===

    /**
     * 네비게이션 방향 계산
     */
    private fun calculateNavigationDirection(location: LocationPoint) {
        val state = _uiState.value
        if (!state.isNavigating || state.routePath.isEmpty()) return

        val currentWaypoint = state.routePath.getOrNull(state.currentWaypointIndex) ?: return

        // 다음 waypoint까지 거리 계산
        val distanceToWaypoint = RouteDeviationMonitor.calculateDistance(
            location.latitude,
            location.longitude,
            currentWaypoint.latitude,
            currentWaypoint.longitude
        )

        // 10m 이내 도착하면 다음 waypoint로
        if (distanceToWaypoint < 10.0) {
            val nextIndex = state.currentWaypointIndex + 1
            if (nextIndex < state.routePath.size) {
                _uiState.update {
                    it.copy(currentWaypointIndex = nextIndex)
                }
                return
            } else {
                // 목적지 도착
                onArrived()
                return
            }
        }

        // 목표 방향 계산 (Bearing)
        val targetBearing = calculateBearing(
            location.latitude,
            location.longitude,
            currentWaypoint.latitude,
            currentWaypoint.longitude
        )

        // 상대 각도 계산 (현재 방향 기준)
        val relativeBearing = (targetBearing - location.bearing + 360) % 360

        // UI 상태 업데이트
        _uiState.update {
            it.copy(
                targetBearing = targetBearing,
                relativeBearing = relativeBearing,
                distanceToNextWaypointMeters = distanceToWaypoint.toInt()
            )
        }
    }

    /**
     * 두 지점 간 방향 계산 (Bearing, 북쪽 기준 0도)
     */
    private fun calculateBearing(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val dLon = Math.toRadians(lon2 - lon1)
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)

        val y = sin(dLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) -
                sin(lat1Rad) * cos(lat2Rad) * cos(dLon)

        val bearing = Math.toDegrees(atan2(y, x))
        return ((bearing + 360) % 360).toFloat()
    }

    // === 네비게이션 제어 ===

    /**
     * 목적지 도착 처리
     */
    private fun onArrived() {
        Log.d(TAG, "=== [WATCH] Arrived at destination")

        _uiState.update {
            it.copy(
                isNavigating = false,
                isArrived = true
            )
        }

        // Phone으로 네비게이션 종료 알림
        viewModelScope.launch {
            try {
                val navigationId = navigationRouteManager.currentRoute.value?.navigationId ?: return@launch
                locationRepository.endNavigation(
                    navigationId = navigationId.toLongOrNull() ?: 0L,
                    endedBy = "PATIENT"
                )
                Log.d(TAG, "✓ Navigation end request sent to Phone")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send navigation end request", e)
            }
        }

        // 경로 초기화
        navigationRouteManager.clearRoute()
    }

    /**
     * 네비게이션 취소
     */
    fun cancelNavigation() {
        Log.d(TAG, "=== [WATCH] Cancel navigation")

        // Phone으로 네비게이션 종료 알림
        viewModelScope.launch {
            try {
                val navigationId = navigationRouteManager.currentRoute.value?.navigationId ?: return@launch
                locationRepository.endNavigation(
                    navigationId = navigationId.toLongOrNull() ?: 0L,
                    endedBy = "PATIENT"
                )
                Log.d(TAG, "✓ Navigation cancel request sent to Phone")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send navigation cancel request", e)
            }
        }

        // 경로 초기화
        navigationRouteManager.clearRoute()
    }

    /**
     * 경로 초기화
     */
    private fun clearRoute() {
        _uiState.update {
            it.copy(
                isNavigating = false,
                destinationName = null,
                routePath = emptyList(),
                currentWaypointIndex = 0,
                isRouteDeviated = false,
                isEmergency = false,
                isArrived = false
            )
        }

        routeDeviationMonitor?.reset()
        routeDeviationMonitor = null
    }

    // === ViewModel 파괴 ===

    override fun onCleared() {
        super.onCleared()
        routeDeviationMonitor?.reset()
        Log.d(TAG, "NavigationViewModel cleared")
    }
}

/**
 * JSON 경로 좌표 파싱용 데이터 클래스
 */
@Serializable
private data class RouteCoordinate(
    val lat: Double,
    val lon: Double
)

/**
 * 네비게이션 UI 상태
 */
data class NavigationUiState(
    // 현재 위치
    val currentLatitude: Double = 0.0,
    val currentLongitude: Double = 0.0,
    val currentBearing: Float = 0f,

    // 네비게이션 상태
    val isNavigating: Boolean = false,
    val destinationName: String? = null,
    val totalDistanceMeters: Int = 0,
    val estimatedTimeMinutes: Int = 0,

    // 경로 정보
    val routePath: List<NavigationRouteManager.LatLng> = emptyList(),
    val currentWaypointIndex: Int = 0,

    // 방향 정보
    val targetBearing: Float = 0f,
    val relativeBearing: Float = 0f,
    val distanceToNextWaypointMeters: Int = 0,

    // 경로 이탈
    val isRouteDeviated: Boolean = false,
    val deviationDistanceMeters: Int = 0,
    val isEmergency: Boolean = false,

    // 도착 여부
    val isArrived: Boolean = false
)
