package kr.co.ongil.presentation.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.domain.repository.UserRepository
import kr.co.ongil.domain.repository.FavoriteRepository
import kr.co.ongil.domain.usecase.dashboard.GetDashboardUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userDataStoreManager: UserDataStoreManager,
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository,
    private val getDashboardUseCase: GetDashboardUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _uiState = MutableStateFlow(
        HomeUiState(
            guardianName = "",
            patientName = "",
            mostVisitedLabel = "가장 많이 방문한 목적지",
            mostVisitedPlace = "집",
            outOfSafeZoneCount = 0,
            routeFailCount = 0
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observePatientIdForDashboard()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                userRepository.getMyInfo(),
                userDataStoreManager.getSelectedPatientId(),
                flow {
                    val result = favoriteRepository.getMyRelationships()
                    emit(result.getOrNull() ?: emptyList())
                }
            ) { userInfoResult, selectedId, patients ->
                Triple(userInfoResult, selectedId, patients)
            }.collect { (userInfoResult, selectedId, patients) ->
                // 로그인한 사용자 이름
                val guardianName = userInfoResult?.getOrNull()?.name ?: ""

                // 선택된 환자의 relationshipName (PatientData.name에 저장됨)
                val selectedPatient = patients.find { it.id.toString() == selectedId }
                val patientName = selectedPatient?.name ?: ""

                _uiState.value = _uiState.value.copy(
                    guardianName = guardianName,
                    patientName = patientName
                )
            }
        }
    }

    // 환자 ID 변경 감지하여 dashboard 데이터 로드
    private fun observePatientIdForDashboard() {
        viewModelScope.launch {
            try {
                // 환자 타입에 따라 적절한 ID Flow 구독
                userDataStoreManager.getUserType().flatMapLatest { userType ->
                    Log.d(TAG, "observePatientIdForDashboard() - userType: $userType")
                    if (userType == "PATIENT") {
                        userDataStoreManager.getLoginUserId()
                    } else {
                        userDataStoreManager.getSelectedPatientId()
                    }
                }.collectLatest { patientIdStr ->
                    Log.d(TAG, "observePatientIdForDashboard() - patientId: $patientIdStr")
                    if (!patientIdStr.isNullOrEmpty()) {
                        loadDashboardData(patientIdStr)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "observePatientIdForDashboard() - 예외 발생", e)
            }
        }
    }

    // Dashboard 데이터 로드
    private fun loadDashboardData(patientIdStr: String) {
        viewModelScope.launch {
            try {
                val patientId = patientIdStr.toIntOrNull()
                if (patientId == null) {
                    Log.e(TAG, "loadDashboardData() - 잘못된 환자 ID: $patientIdStr")
                    return@launch
                }

                Log.d(TAG, "loadDashboardData() - dashboard 데이터 로드 시작 (patientId: $patientId)")

                getDashboardUseCase(patientId).collectLatest { result ->
                    result.onSuccess { dashboardDto ->
                        Log.d(TAG, "loadDashboardData() - dashboard 조회 성공: $dashboardDto")

                        _uiState.value = _uiState.value.copy(
                            mostVisitedPlace = dashboardDto.favoriteName ?: "", // 빈 문자열로 전달 (UI에서 처리)
                            outOfSafeZoneCount = dashboardDto.safezoneExit,
                            routeFailCount = dashboardDto.routeLost
                        )
                    }.onFailure { exception ->
                        Log.e(TAG, "loadDashboardData() - dashboard 조회 실패", exception)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadDashboardData() - 예외 발생", e)
            }
        }
    }
}
