package kr.co.ongil.presentation.ui.safezonesetting

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.domain.usecase.safezone.GetAndSaveSafeZoneUseCase
import kr.co.ongil.domain.usecase.safezone.UpdateAndSaveSafeZoneUseCase
import javax.inject.Inject

// 안전구역 설정 전반 관리
@HiltViewModel
class SafeZoneSettingViewModel @Inject constructor(
    private val userDataStoreManager: UserDataStoreManager,
    private val getAndSaveSafeZoneUseCase: GetAndSaveSafeZoneUseCase,
    private val updateAndSaveSafeZoneUseCase: UpdateAndSaveSafeZoneUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "SafeZoneSettingViewModel"
    }

    private val _uiState = MutableStateFlow(SafeZoneUiState())
    val uiState: StateFlow<SafeZoneUiState> = _uiState

    init {
        // 선택된 환자 ID 변경 감지
        viewModelScope.launch {
            userDataStoreManager.getSelectedPatientId().collect { patientId ->
//                Log.d(TAG, "선택된 환자 ID 변경: $patientId")
                if (patientId != null) {
                    loadSettings(patientId)
                } else {
//                    Log.d(TAG, "선택된 환자가 없어 기본값 사용")
                    // 환자가 선택되지 않은 경우 기본값 유지
                }
            }
        }
    }


    fun onEvent(event: SafeZoneSettingUiEvent) {
        when (event) {
            is SafeZoneSettingUiEvent.ChangeLevelDistance -> {
                updateLevelDistance(event.level, event.meters)
            }
            is SafeZoneSettingUiEvent.ChangeLevelDwell -> {
                updateLevelDwell(event.level, event.minutes)
            }
            is SafeZoneSettingUiEvent.TogglePush -> {
                _uiState.update { it.copy(pushEnabled = event.enabled) }
            }
            is SafeZoneSettingUiEvent.ToggleAutoCall -> {
                _uiState.update { it.copy(autoCallEnabled = event.enabled) }
            }
            is SafeZoneSettingUiEvent.Save -> {
                saveSettings( onSuccess = event.onSuccess)
            }
        }
    }

    // 단계 별 배회 탐지 거리 설정
    private fun updateLevelDistance(level: Int, meters: Int) {
        _uiState.update { currentState ->
            when (level) {
                1 -> currentState.copy(
                    level1 = currentState.level1.copy(distanceMeters = meters)
                )
                2 -> currentState.copy(
                    level2 = currentState.level2.copy(distanceMeters = meters)
                )
                3 -> currentState.copy(
                    level3 = currentState.level3.copy(distanceMeters = meters)
                )
                else -> currentState
            }
        }
    }

    // 단계 별 배회 시간 설정
    private fun updateLevelDwell(level: Int, minutes: Int) {
        _uiState.update { currentState ->
            when (level) {
                1 -> currentState.copy(
                    level1 = currentState.level1.copy(dwellMinutes = minutes)
                )
                2 -> currentState.copy(
                    level2 = currentState.level2.copy(dwellMinutes = minutes)
                )
                3 -> currentState.copy(
                    level3 = currentState.level3.copy(dwellMinutes = minutes)
                )
                else -> currentState
            }
        }
    }

    // 설정 저장해서 api
    private fun saveSettings(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "saveSettings() - 시작")
                _uiState.update { it.copy(isLoading = true) }

                val state = _uiState.value
                val selectedPatientId = userDataStoreManager.getSelectedPatientId().first()

                if (selectedPatientId == null) {
                    Log.e(TAG, "❌ 선택된 환자 ID가 없습니다.")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "환자를 선택해주세요."
                        )
                    }
                    return@launch
                }

                // UseCase로 업데이트 및 저장
                val result = updateAndSaveSafeZoneUseCase(
                    patientId = selectedPatientId.toLong(),
                    level1Distance = state.level1.distanceMeters,
                    level1Dwell = state.level1.dwellMinutes,
                    level2Distance = state.level2.distanceMeters,
                    level2Dwell = state.level2.dwellMinutes,
                    level3Distance = state.level3.distanceMeters,
                    level3Dwell = state.level3.dwellMinutes,
                    pushEnabled = state.pushEnabled,
                    autoCallEnabled = state.autoCallEnabled
                )

                result.onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    onSuccess()
//                    Log.d(TAG, "✅ 전체 저장 완료")
                }.onFailure { error ->
                    Log.e(TAG, "❌ 저장 실패: ${error.message}", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "설정 저장에 실패했습니다: ${error.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "설정 저장 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "설정 저장에 실패했습니다."
                    )
                }
            }
        }
    }
    // 서버에서 기존 설정 불러오는거
    private fun loadSettings(patientId: String) {
        viewModelScope.launch {
            try {
//                Log.d(TAG, "loadSettings() - 시작")
//                Log.d(TAG, "  - patientId: $patientId")
                _uiState.update { it.copy(isLoading = true) }

                // UseCase로 백엔드 조회 및 DataStore 저장
                val result = getAndSaveSafeZoneUseCase(patientId.toLong())

                result.onSuccess {
                    // DataStore에서 저장된 값을 읽어와서 UI 업데이트
                    val settings = userDataStoreManager.getSafeZoneSettings(patientId.toLong())
                    _uiState.update {
                        it.copy(
                            level1 = SafeZoneLevelConfig(
                                distanceMeters = settings.level1Distance,
                                dwellMinutes = settings.level1Dwell
                            ),
                            level2 = SafeZoneLevelConfig(
                                distanceMeters = settings.level2Distance,
                                dwellMinutes = settings.level2Dwell
                            ),
                            level3 = SafeZoneLevelConfig(
                                distanceMeters = settings.level3Distance,
                                dwellMinutes = settings.level3Dwell
                            ),
                            pushEnabled = settings.pushEnabled,
                            autoCallEnabled = settings.autoCallEnabled,
                            isLoading = false,
                            error = null
                        )
                    }
//                    Log.d(TAG, "✅ 설정값 불러오기 완료")
                }.onFailure { error ->
                    Log.e(TAG, "❌ 설정값 불러오기 실패: ${error.message}")
                    // 기본값 사용
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "설정 불러오기 실패", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }
}

/**
 * SafeZone 설정 데이터 클래스
 */
data class SafeZoneSettings(
    val level1Distance: Int = 100,
    val level1Dwell: Int = 60,
    val level2Distance: Int = 350,
    val level2Dwell: Int = 30,
    val level3Distance: Int = 700,
    val level3Dwell: Int = 15,
    val pushEnabled: Boolean = true,
    val autoCallEnabled: Boolean = false
)
