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
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.common.location.SafetyZoneMonitor
import javax.inject.Inject

/**
 * 대시보드 ViewModel
 *
 * 주요 기능:
 * 1. 위치 추적 상태 관리
 * 2. 안전 범위 상태 관리
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val locationStreamBus: LocationStreamBus,
    private val safetyZoneMonitor: SafetyZoneMonitor
) : ViewModel() {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    // === UI 상태 ===

    data class UiState(
        val locationTrackingActive: Boolean = false,
        val insideSafeZone: Boolean = true,
        val currentLatitude: Double? = null,
        val currentLongitude: Double? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeLocationUpdates()
    }

    /**
     * 위치 업데이트 관찰
     */
    private fun observeLocationUpdates() {
        viewModelScope.launch {
            locationStreamBus.updates.collect { locationPoint ->
                Log.d(TAG, "위치 업데이트 수신: ${locationPoint.latitude}, ${locationPoint.longitude}")

                // 위치 추적 활성화 표시
                _uiState.update {
                    it.copy(
                        locationTrackingActive = true,
                        currentLatitude = locationPoint.latitude,
                        currentLongitude = locationPoint.longitude
                    )
                }

                // 안전 범위 상태 확인
                val safetyStatus = safetyZoneMonitor.getCurrentStatus()
                _uiState.update {
                    it.copy(
                        insideSafeZone = safetyStatus.currentStage == 0 // Stage 0 = 안전 범위 내
                    )
                }
            }
        }
    }

    /**
     * 마지막 알려진 위치 확인
     */
    fun checkLastKnownLocation() {
        val lastLocation = locationStreamBus.lastKnownLocation
        if (lastLocation != null) {
            _uiState.update {
                it.copy(
                    locationTrackingActive = true,
                    currentLatitude = lastLocation.latitude,
                    currentLongitude = lastLocation.longitude
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    locationTrackingActive = false
                )
            }
        }
    }
}
