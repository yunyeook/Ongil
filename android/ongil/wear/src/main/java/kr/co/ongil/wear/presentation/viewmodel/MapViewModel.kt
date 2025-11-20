package kr.co.ongil.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.wear.domain.model.SafeZoneConfig
import kr.co.ongil.wear.domain.model.SafeZoneStatus
import kr.co.ongil.wear.domain.usecase.MonitorSafeZoneUseCase
import kr.co.ongil.wear.domain.usecase.TrackLocationUseCase
import javax.inject.Inject

/**
 * 지도 화면 ViewModel
 *
 * 주요 기능:
 * 1. 위치 추적 시작/중지
 * 2. 실시간 위치 업데이트
 * 3. 안전 범위 모니터링
 * 4. 지도 UI 상태 관리
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val trackLocationUseCase: TrackLocationUseCase,
    private val monitorSafeZoneUseCase: MonitorSafeZoneUseCase,
    private val locationStreamBus: LocationStreamBus
) : ViewModel() {

    companion object {
        private const val TAG = "MapViewModel"
    }

    // UI State
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        // 위치 업데이트 구독
        observeLocationUpdates()

        // 안전 범위 모니터링 시작 (기본값으로)
        // TODO: DataStore에서 저장된 안전 범위 설정 로드
        startSafeZoneMonitoring(37.5665, 126.9780) // 서울 시청 기본값
    }

    /**
     * 위치 추적 시작
     */
    fun startLocationTracking() {
        trackLocationUseCase.startTracking()
        _uiState.update { it.copy(isTrackingActive = true) }
    }

    /**
     * 위치 추적 중지
     */
    fun stopLocationTracking() {
        trackLocationUseCase.stopTracking()
        _uiState.update { it.copy(isTrackingActive = false) }
    }

    /**
     * 위치 업데이트 구독
     */
    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationStreamBus.updates.collect { locationPoint ->
                _uiState.update {
                    it.copy(
                        currentLatitude = locationPoint.latitude,
                        currentLongitude = locationPoint.longitude,
                        accuracy = locationPoint.accuracyMeters,
                        bearing = locationPoint.bearing,
                        speed = locationPoint.speedMps
                    )
                }
            }
        }
    }

    /**
     * 안전 범위 모니터링 시작
     */
    private fun startSafeZoneMonitoring(homeLatitude: Double, homeLongitude: Double) {
        viewModelScope.launch {
            monitorSafeZoneUseCase(homeLatitude, homeLongitude).collect { safeZoneStatus ->
                _uiState.update {
                    it.copy(safeZoneStatus = safeZoneStatus)
                }
            }
        }
    }

    /**
     * 안전 범위 설정 업데이트
     */
    fun updateSafeZoneConfig(config: SafeZoneConfig) {
        _uiState.update {
            it.copy(safeZoneConfig = config)
        }

        // SafeZoneMonitor 업데이트
        monitorSafeZoneUseCase.updateSafeZone(
            homeLatitude = config.homeLatitude,
            homeLongitude = config.homeLongitude,
            stage1Radius = config.stage1Radius,
            stage2Radius = config.stage2Radius,
            stage3Radius = config.stage3Radius
        )

        // 안전 범위 모니터링 재시작
        startSafeZoneMonitoring(config.homeLatitude, config.homeLongitude)
    }

    /**
     * 지도 줌 레벨 변경
     */
    fun setZoomLevel(zoomLevel: Int) {
        _uiState.update { it.copy(zoomLevel = zoomLevel) }
    }
}

/**
 * 지도 UI 상태
 */
data class MapUiState(
    // 위치 추적 상태
    val isTrackingActive: Boolean = false,

    // 현재 위치
    val currentLatitude: Double? = null,
    val currentLongitude: Double? = null,
    val accuracy: Float? = null,
    val bearing: Float? = null,
    val speed: Float? = null,

    // 안전 범위 설정
    val safeZoneConfig: SafeZoneConfig = SafeZoneConfig(
        homeLatitude = 37.5665,
        homeLongitude = 126.9780,
        stage1Radius = 100,
        stage2Radius = 350,
        stage3Radius = 700
    ),

    // 안전 범위 상태
    val safeZoneStatus: SafeZoneStatus = SafeZoneStatus(),

    // 지도 설정
    val zoomLevel: Int = 15,

    // 에러 상태
    val errorMessage: String? = null
) {
    /**
     * 현재 위치가 있는지 여부
     */
    val hasLocation: Boolean
        get() = currentLatitude != null && currentLongitude != null

    /**
     * 안전 범위 내에 있는지 여부
     */
    val isInsideSafeZone: Boolean
        get() = safeZoneStatus.isInsideStage1 &&
                safeZoneStatus.isInsideStage2 &&
                safeZoneStatus.isInsideStage3
}