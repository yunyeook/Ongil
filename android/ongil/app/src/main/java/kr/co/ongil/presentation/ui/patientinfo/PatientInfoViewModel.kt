package kr.co.ongil.presentation.ui.patientinfo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.domain.usecase.patientinfo.GetPatientInfoUseCase
import javax.inject.Inject

@HiltViewModel
class PatientInfoViewModel @Inject constructor(
    private val getPatientInfoUseCase: GetPatientInfoUseCase,
    private val userDataStoreManager: UserDataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientInfoUiState())
    val uiState: StateFlow<PatientInfoUiState> = _uiState.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val TAG = "PatientInfoViewModel"
    }

    init {
        observePatientIdChanges()
    }

    // 환자 ID 변경 감지
    private fun observePatientIdChanges() {
        viewModelScope.launch {
            try {
                val userType = userDataStoreManager.getUserType().firstOrNull()
                Log.d(TAG, "observePatientIdChanges() - userType: $userType")

                // 환자 타입에 따라 적절한 ID Flow 구독
                if (userType == "PATIENT") {
                    // 환자는 자신의 ID만 사용 (변경 없음)
                    userDataStoreManager.getLoginUserId().collectLatest { patientIdStr ->
                        Log.d(TAG, "observePatientIdChanges() - PATIENT ID: $patientIdStr")
                        if (!patientIdStr.isNullOrEmpty()) {
                            loadPatientInfo(patientIdStr)
                        }
                    }
                } else {
                    // 보호자는 선택된 환자 ID를 감시 (변경 감지)
                    userDataStoreManager.getSelectedPatientId().collectLatest { patientIdStr ->
                        Log.d(TAG, "observePatientIdChanges() - GUARDIAN selected patient ID: $patientIdStr")
                        if (!patientIdStr.isNullOrEmpty()) {
                            loadPatientInfo(patientIdStr)
                        } else {
                            _uiState.value = PatientInfoUiState(
                                isLoading = false,
                                error = "선택된 환자가 없습니다."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "observePatientIdChanges() - 예외 발생", e)
                _uiState.value = PatientInfoUiState(
                    isLoading = false,
                    error = "사용자 정보를 불러오는데 실패했습니다."
                )
            }
        }
    }

    // 환자 정보 로드
    private fun loadPatientInfo(patientIdStr: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val patientId = patientIdStr.toIntOrNull()
                if (patientId == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "잘못된 환자 ID입니다."
                    )
                    return@launch
                }

                Log.d(TAG, "loadPatientInfo() - 환자 정보 로드 시작 (patientId: $patientId)")

                getPatientInfoUseCase(patientId).collectLatest { result ->
                    result.onSuccess { patientInfoDto ->
                        Log.d(TAG, "loadPatientInfo() - 환자 정보 조회 성공: $patientInfoDto")

                        try {
                            // favorite JSON 파싱
                            val favoriteLocations = try {
                                val parsed = json.decodeFromString<List<FavoriteLocation>>(patientInfoDto.favorite)
                                Log.d(TAG, "favorite 파싱 성공: $parsed")
                                parsed
                            } catch (e: Exception) {
                                Log.e(TAG, "favorite 파싱 실패: ${patientInfoDto.favorite}", e)
                                emptyList()
                            }

                            // safezoneExit JSON 파싱
                            val safezoneExitMap = try {
                                val parsed = json.decodeFromString<Map<String, Int>>(patientInfoDto.safezoneExit)
                                Log.d(TAG, "safezoneExit 파싱 성공: $parsed")
                                parsed
                            } catch (e: Exception) {
                                Log.e(TAG, "safezoneExit 파싱 실패: ${patientInfoDto.safezoneExit}", e)
                                emptyMap()
                            }

                            val activityLog = ActivityLog(
                                favoriteLocations = favoriteLocations,
                                safezoneExit = safezoneExitMap,
                                routeLost = patientInfoDto.routeLost,
                                routeLostDiff = patientInfoDto.routeLostDiff,
                                routeTransition = patientInfoDto.routeTransition,
                                safezoneEmer = patientInfoDto.safezoneEmer,
                                safezoneEmerDiff = patientInfoDto.safezoneEmerDiff,
                                safezoneTransition = patientInfoDto.safezoneTransition,
                                sosSign = patientInfoDto.sosSign,
                                sosSignDiff = patientInfoDto.sosSignDiff,
                                sosSignTransition = patientInfoDto.sosSignTransition,
                                emerCall = patientInfoDto.emerCall,
                                emerCallDiff = patientInfoDto.emerCallDiff,
                                emerCallTransition = patientInfoDto.emerCallTransition
                            )

                            _uiState.value = PatientInfoUiState(
                                isLoading = false,
                                activityLog = activityLog
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "데이터 파싱 중 오류", e)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "데이터 파싱에 실패했습니다: ${e.message}"
                            )
                        }
                    }.onFailure { exception ->
                        Log.e(TAG, "loadPatientInfo() - 환자 정보 조회 실패", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "환자 정보를 불러오는데 실패했습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadPatientInfo() - 예외 발생", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "알 수 없는 오류가 발생했습니다."
                )
            }
        }
    }
}
