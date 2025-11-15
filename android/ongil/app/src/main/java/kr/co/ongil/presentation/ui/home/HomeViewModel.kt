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
import kr.co.ongil.domain.repository.HealthConnectRepository
import kr.co.ongil.domain.usecase.dashboard.GetDashboardUseCase
import kr.co.ongil.domain.usecase.patientinfo.GetPatientInfoUseCase
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kr.co.ongil.presentation.ui.patientinfo.ActivityLog
import kr.co.ongil.presentation.ui.patientinfo.FavoriteLocation
import kr.co.ongil.common.location.LocationStreamBus
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userDataStoreManager: UserDataStoreManager,
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository,
    private val getDashboardUseCase: GetDashboardUseCase,
    private val healthConnectRepository: HealthConnectRepository,
    private val getPatientInfoUseCase: GetPatientInfoUseCase,
    val locationBus: LocationStreamBus
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _uiState = MutableStateFlow(
        HomeUiState(
            guardianName = "",
            patientName = "",
            mostVisitedLabel = "가장 많이 방문한 목적지",
            mostVisitedPlace = "집",
            outOfSafeZoneCount = 0,
            routeFailCount = 0,
            averageSleepHours = null,
            averageSteps = null,
            activityLog = null
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observePatientIdForDashboard()
        observeHealthData()
        observePatientIdForActivityLog()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            userDataStoreManager.getUserType().collectLatest { userType ->
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

                    // 환자 이름: 환자 로그인 시 자기 이름, 보호자 로그인 시 선택된 환자 이름
                    val patientName = if (userType == "PATIENT") {
                        guardianName // 환자가 로그인한 경우 자기 이름
                    } else {
                        // 선택된 환자의 relationshipName (PatientData.name에 저장됨)
                        val selectedPatient = patients.find { it.id.toString() == selectedId }
                        selectedPatient?.name ?: ""
                    }

                    _uiState.value = _uiState.value.copy(
                        guardianName = guardianName,
                        patientName = patientName
                    )
                }
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

    // 건강 데이터 관찰
    private fun observeHealthData() {
        viewModelScope.launch {
            try {
                healthConnectRepository.getHealthData().collectLatest { result ->
                    result.onSuccess { healthData ->
                        Log.d(TAG, "observeHealthData() - 건강 데이터 조회 성공: $healthData")

                        _uiState.value = _uiState.value.copy(
                            averageSleepHours = healthData.sleep?.average,
                            averageSteps = healthData.steps?.average?.toInt(),
                            healthData = healthData
                        )
                    }.onFailure { exception ->
                        Log.e(TAG, "observeHealthData() - 건강 데이터 조회 실패", exception)
                        // 실패 시 null 유지
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "observeHealthData() - 예외 발생", e)
            }
        }
    }

    // 환자 ID 변경 감지하여 활동 로그 데이터 로드
    private fun observePatientIdForActivityLog() {
        viewModelScope.launch {
            try {
                userDataStoreManager.getUserType().flatMapLatest { userType ->
                    Log.d(TAG, "observePatientIdForActivityLog() - userType: $userType")
                    if (userType == "PATIENT") {
                        userDataStoreManager.getLoginUserId()
                    } else {
                        userDataStoreManager.getSelectedPatientId()
                    }
                }.collectLatest { patientIdStr ->
                    Log.d(TAG, "observePatientIdForActivityLog() - patientId: $patientIdStr")
                    if (!patientIdStr.isNullOrEmpty()) {
                        loadActivityLogData(patientIdStr)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "observePatientIdForActivityLog() - 예외 발생", e)
            }
        }
    }

    // 활동 로그 데이터 로드
    private fun loadActivityLogData(patientIdStr: String) {
        viewModelScope.launch {
            try {
                val patientId = patientIdStr.toIntOrNull()
                if (patientId == null) {
                    Log.e(TAG, "loadActivityLogData() - 잘못된 환자 ID: $patientIdStr")
                    return@launch
                }

                Log.d(TAG, "loadActivityLogData() - 활동 로그 데이터 로드 시작 (patientId: $patientId)")

                getPatientInfoUseCase(patientId).collectLatest { result ->
                    result.onSuccess { patientInfoDto ->
                        Log.d(TAG, "loadActivityLogData() - 환자 정보 조회 성공: $patientInfoDto")

                        try {
                            // favorite JSON 파싱
                            val favoriteLocations = try {
                                json.decodeFromString<List<FavoriteLocation>>(patientInfoDto.favorite)
                            } catch (e: Exception) {
                                Log.e(TAG, "favorite 파싱 실패: ${patientInfoDto.favorite}", e)
                                emptyList()
                            }

                            // safezoneExit JSON 파싱
                            val safezoneExitMap = try {
                                json.decodeFromString<Map<String, Int>>(patientInfoDto.safezoneExit)
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

                            _uiState.value = _uiState.value.copy(activityLog = activityLog)
                        } catch (e: Exception) {
                            Log.e(TAG, "데이터 파싱 중 오류", e)
                        }
                    }.onFailure { exception ->
                        Log.e(TAG, "loadActivityLogData() - 환자 정보 조회 실패", exception)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadActivityLogData() - 예외 발생", e)
            }
        }
    }
}
