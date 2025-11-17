package kr.co.ongil.presentation.ui.patientinfo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.mapper.toDomain
import kr.co.ongil.domain.repository.HealthConnectRepository
import kr.co.ongil.domain.repository.PatientInsightRepository
import kr.co.ongil.domain.model.HealthDataType
import kr.co.ongil.domain.usecase.health.DeleteHealthDataUseCase
import kr.co.ongil.domain.usecase.health.GetHealthDataFromServerUseCase
import kr.co.ongil.domain.usecase.health.GetHealthDataSummaryUseCase
import kr.co.ongil.domain.usecase.health.GetHealthDataUseCase
import kr.co.ongil.domain.usecase.health.UploadHealthDataUseCase
import kr.co.ongil.domain.usecase.patientinfo.GetPatientInfoUseCase
import javax.inject.Inject

@HiltViewModel
class PatientInfoViewModel @Inject constructor(
    private val getPatientInfoUseCase: GetPatientInfoUseCase,
    private val getHealthDataUseCase: GetHealthDataUseCase,
    private val uploadHealthDataUseCase: UploadHealthDataUseCase,
    private val getHealthDataFromServerUseCase: GetHealthDataFromServerUseCase,
    private val getHealthDataSummaryUseCase: GetHealthDataSummaryUseCase,
    private val deleteHealthDataUseCase: DeleteHealthDataUseCase,
    private val healthConnectRepository: HealthConnectRepository,
    private val userDataStoreManager: UserDataStoreManager,
    private val patientInsightRepository: PatientInsightRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientInfoUiState())
    val uiState: StateFlow<PatientInfoUiState> = _uiState.asStateFlow()

    private var currentPatientId: Long? = null

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    companion object {
        private const val TAG = "PatientInfoViewModel"
    }

    init {
        observePatientIdChanges()
        checkHealthPermissions()
        observeUserTypeAndPatientId()
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
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "선택된 환자가 없습니다.",
                                activityLog = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "observePatientIdChanges() - 예외 발생", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "사용자 정보를 불러오는데 실패했습니다.",
                    activityLog = null
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

                // 현재 환자 ID 저장
                currentPatientId = patientId.toLong()
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

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                activityLog = activityLog,
                                error = null
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

    // Health Connect 권한 체크
    private fun checkHealthPermissions() {
        viewModelScope.launch {
            try {
                val hasPermission = healthConnectRepository.checkPermissions()
                Log.d(TAG, "checkHealthPermissions() - 권한 상태: $hasPermission")
                _uiState.value = _uiState.value.copy(healthPermissionGranted = hasPermission)

                if (hasPermission) {
                    loadHealthData()
                }
            } catch (e: Exception) {
                Log.e(TAG, "checkHealthPermissions() - 권한 체크 실패", e)
                _uiState.value = _uiState.value.copy(healthPermissionGranted = false)
            }
        }
    }

    // 권한 요청할 권한 목록 가져오기
    suspend fun getPermissionsToRequest(): Set<String> {
        return healthConnectRepository.getPermissionsToRequest()
    }

    // 권한 요청 결과 처리
    fun onPermissionResult() {
        checkHealthPermissions()
    }

    // 건강 데이터 로드 + 서버 동기화
    private fun loadHealthData() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "loadHealthData() - 건강 데이터 로드 시작")

                getHealthDataUseCase().collectLatest { result ->
                    result.onSuccess { localHealthData ->
                        Log.d(TAG, "loadHealthData() - 건강 데이터 조회 성공: $localHealthData")
                        _uiState.value = _uiState.value.copy(healthData = localHealthData)

                        // 🔁 서버에 동기화
                        val pid = currentPatientId
                        if (pid != null && localHealthData != null) {
                            launch {
                                // LocalHealthData → 도메인 모델로 변환
                                val domainData = localHealthData.toDomain()

                                uploadHealthDataUseCase(pid, domainData)
                                    .onSuccess { message ->
                                        Log.d(TAG, "HealthData 서버 업로드 성공: $message")
                                    }
                                    .onFailure { e ->
                                        Log.e(TAG, "HealthData 서버 업로드 실패", e)
                                    }
                            }
                        }
                    }.onFailure { exception ->
                        Log.e(TAG, "loadHealthData() - 건강 데이터 조회 실패", exception)
                        // 건강 데이터 조회 실패는 치명적이지 않으므로 에러 표시하지 않음
                        _uiState.value = _uiState.value.copy(healthData = null)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadHealthData() - 예외 발생", e)
                _uiState.value = _uiState.value.copy(healthData = null)
            }
        }
    }

    // 사용자 타입과 환자 ID 관찰
    private fun observeUserTypeAndPatientId() {
        viewModelScope.launch {
            try {
                // 먼저 userType 로드
                userDataStoreManager.getUserType().collectLatest { userType ->
                    Log.d(TAG, "observeUserTypeAndPatientId() - userType: $userType")
                    _uiState.value = _uiState.value.copy(userType = userType ?: "")

                    // userType에 따라 적절한 환자 ID로 인사이트 로드
                    if (userType == "PATIENT") {
                        userDataStoreManager.getLoginUserId().firstOrNull()?.let { patientIdStr ->
                            if (patientIdStr.isNotEmpty()) {
                                loadInsightData(patientIdStr)
                            }
                        }
                    } else {
                        userDataStoreManager.getSelectedPatientId().firstOrNull()?.let { patientIdStr ->
                            if (patientIdStr.isNotEmpty()) {
                                loadInsightData(patientIdStr)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "observeUserTypeAndPatientId() - 예외 발생", e)
            }
        }
    }

    // 인사이트 데이터 로드
    private fun loadInsightData(patientIdStr: String) {
        viewModelScope.launch {
            try {
                val patientId = patientIdStr.toIntOrNull()
                if (patientId == null) {
                    Log.e(TAG, "loadInsightData() - 잘못된 환자 ID: $patientIdStr")
                    return@launch
                }

                Log.d(TAG, "loadInsightData() - 인사이트 데이터 로드 시작 (patientId: $patientId)")

                patientInsightRepository.getLatestWeeklyInsight(patientId).collectLatest { result ->
                    result.onSuccess { insightDto ->
                        Log.d(TAG, "loadInsightData() - 인사이트 조회 성공: $insightDto")

                        _uiState.value = _uiState.value.copy(
                            summary = insightDto.summary,
                            positiveSignals = insightDto.positiveSignals,
                            warningSignals = insightDto.warningSignals,
                            caregiverSuggestions = insightDto.caregiverSuggestions
                        )
                    }.onFailure { exception ->
                        Log.e(TAG, "loadInsightData() - 인사이트 조회 실패", exception)
                        // 실패 시 빈 리스트 유지
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadInsightData() - 예외 발생", e)
            }
        }
    }

    // 수동 동기화 함수 (버튼으로 호출 가능)
    fun syncHealthDataToServer() {
        viewModelScope.launch {
            val pid = currentPatientId
            if (pid == null) {
                Log.w(TAG, "syncHealthDataToServer() - 환자 ID가 없습니다")
                return@launch
            }

            val localHealthData = _uiState.value.healthData
            if (localHealthData == null) {
                Log.w(TAG, "syncHealthDataToServer() - 건강 데이터가 없습니다")
                return@launch
            }

            try {
                // LocalHealthData → 도메인 모델로 변환
                val domainData = localHealthData.toDomain()

                uploadHealthDataUseCase(pid, domainData)
                    .onSuccess { message ->
                        Log.d(TAG, "수동 동기화 성공: $message")
                        // TODO: UI 업데이트 (Toast 등)
                    }
                    .onFailure { e ->
                        Log.e(TAG, "수동 동기화 실패", e)
                        // TODO: 에러 처리
                    }
            } catch (e: Exception) {
                Log.e(TAG, "syncHealthDataToServer() - 예외 발생", e)
            }
        }
    }

    /**
     * 서버에서 건강 데이터 조회
     * @param type 조회할 데이터 종류 (null이면 전체 조회)
     * @param from 조회 시작 날짜 (yyyyMMdd)
     * @param to 조회 종료 날짜 (yyyyMMdd)
     * @param sort 정렬 기준 (기본: measuredAt,desc)
     */
    fun getHealthDataFromServer(
        type: HealthDataType? = null,
        from: String? = null,
        to: String? = null,
        sort: String = "measuredAt,desc"
    ) {
        viewModelScope.launch {
            val pid = currentPatientId
            if (pid == null) {
                Log.w(TAG, "getHealthDataFromServer() - 환자 ID가 없습니다")
                return@launch
            }

            try {
                getHealthDataFromServerUseCase(
                    patientId = pid,
                    type = type,
                    from = from,
                    to = to,
                    sort = sort
                ).onSuccess { response ->
                    Log.d(TAG, "서버에서 건강 데이터 조회 성공: ${response.data.records.size}개 레코드")
                    Log.d(TAG, "응답 데이터: ${response.data}")
                    // TODO: UI 업데이트 (필요시 UiState에 서버 데이터 필드 추가)
                }.onFailure { exception ->
                    Log.e(TAG, "서버에서 건강 데이터 조회 실패", exception)
                    // TODO: 에러 처리
                }
            } catch (e: Exception) {
                Log.e(TAG, "getHealthDataFromServer() - 예외 발생", e)
            }
        }
    }

    /**
     * 서버에서 건강 데이터 요약 통계 조회 (일별 단위)
     * @param type 통계할 데이터 종류 (null이면 전체)
     * @param from 조회 시작 날짜 (yyyyMMdd)
     * @param to 조회 종료 날짜 (yyyyMMdd)
     */
    fun getHealthDataSummary(
        type: HealthDataType? = null,
        from: String? = null,
        to: String? = null
    ) {
        viewModelScope.launch {
            val pid = currentPatientId
            if (pid == null) {
                Log.w(TAG, "getHealthDataSummary() - 환자 ID가 없습니다")
                return@launch
            }

            try {
                getHealthDataSummaryUseCase(
                    patientId = pid,
                    type = type,
                    from = from,
                    to = to
                ).onSuccess { response ->
                    Log.d(TAG, "건강 데이터 요약 조회 성공: ${response.data.summary.size}개 일별 요약")
                    Log.d(TAG, "요약 데이터: ${response.data}")
                    // TODO: UI 업데이트 (필요시 UiState에 요약 데이터 필드 추가)
                }.onFailure { exception ->
                    Log.e(TAG, "건강 데이터 요약 조회 실패", exception)
                    // TODO: 에러 처리
                }
            } catch (e: Exception) {
                Log.e(TAG, "getHealthDataSummary() - 예외 발생", e)
            }
        }
    }

    /**
     * 건강 데이터 삭제
     * @param healthDataId 삭제할 건강 데이터 ID
     */
    fun deleteHealthData(healthDataId: Long) {
        viewModelScope.launch {
            val pid = currentPatientId
            if (pid == null) {
                Log.w(TAG, "deleteHealthData() - 환자 ID가 없습니다")
                return@launch
            }

            try {
                deleteHealthDataUseCase(
                    patientId = pid,
                    healthDataId = healthDataId
                ).onSuccess { message ->
                    Log.d(TAG, "건강 데이터 삭제 성공: $message")
                    // TODO: UI 업데이트 (Toast 등)
                    // TODO: 데이터 목록 새로고침
                }.onFailure { exception ->
                    Log.e(TAG, "건강 데이터 삭제 실패", exception)
                    // TODO: 에러 처리
                }
            } catch (e: Exception) {
                Log.e(TAG, "deleteHealthData() - 예외 발생", e)
            }
        }
    }

    /**
     * 건강정보 불러오기 (삼성헬스 조회 → 백엔드 POST → 백엔드 GET)
     * 건강정보 탭의 "건강정보 불러오기" 버튼에서 호출
     */
    fun fetchAndSyncHealthData() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "fetchAndSyncHealthData() - 건강정보 불러오기 시작")
                _uiState.value = _uiState.value.copy(
                    isLoadingHealthData = true,
                    healthSyncMessage = null
                )

                val pid = currentPatientId
                if (pid == null) {
                    Log.w(TAG, "fetchAndSyncHealthData() - 환자 ID가 없습니다")
                    _uiState.value = _uiState.value.copy(
                        isLoadingHealthData = false,
                        healthSyncMessage = "환자 ID를 찾을 수 없습니다"
                    )
                    return@launch
                }

                // 1단계: 삼성헬스에서 건강 데이터 조회
                Log.d(TAG, "fetchAndSyncHealthData() - 1단계: 삼성헬스 데이터 조회")

                val result = getHealthDataUseCase().    first()
                val localHealthData = result.getOrNull()

                if (localHealthData == null) {
                    Log.e(TAG, "fetchAndSyncHealthData() - 삼성헬스 데이터를 가져올 수 없습니다")
                    val error = result.exceptionOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoadingHealthData = false,
                        healthSyncMessage = "삼성헬스 데이터를 가져올 수 없습니다${if (error != null) ": ${error.message}" else ""}"
                    )
                    return@launch
                }

                Log.d(TAG, "fetchAndSyncHealthData() - 삼성헬스 데이터 조회 성공: $localHealthData")

                // 데이터가 비어있는지 확인
                val data = localHealthData // 스마트 캐스팅을 위한 변수
                val hasData = data.heartRate != null ||
                              data.oxygenSaturation != null ||
                              data.sleep != null ||
                              data.steps != null

                if (!hasData) {
                    Log.w(TAG, "fetchAndSyncHealthData() - 삼성헬스에 데이터가 없습니다")
                    _uiState.value = _uiState.value.copy(
                        healthData = data,
                        isLoadingHealthData = false,
                        healthSyncMessage = "삼성헬스에 건강 데이터가 없습니다.\nSamsung Health 앱에서 먼저 데이터를 측정해주세요."
                    )
                    return@launch
                }

                // 2단계: 백엔드에 POST
                Log.d(TAG, "fetchAndSyncHealthData() - 2단계: 백엔드에 POST")
                val domainData = data.toDomain()
                uploadHealthDataUseCase(pid, domainData)
                    .onSuccess { msg: String ->
                        Log.d(TAG, "fetchAndSyncHealthData() - 백엔드 업로드 성공: $msg")

                        // 3단계: 로컬 데이터를 UI에 업데이트
                        _uiState.value = _uiState.value.copy(
                            healthData = data,
                            isLoadingHealthData = false,
                            healthSyncMessage = "건강정보를 성공적으로 불러왔습니다"
                        )

                        // 성공 메시지 3초 후 자동 제거
                        launch {
                            kotlinx.coroutines.delay(3000)
                            _uiState.value = _uiState.value.copy(healthSyncMessage = null)
                        }
                    }
                    .onFailure { exception ->
                        Log.e(TAG, "fetchAndSyncHealthData() - 백엔드 업로드 실패", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoadingHealthData = false,
                            healthSyncMessage = "서버 업로드 실패: ${exception.message}"
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "fetchAndSyncHealthData() - 예외 발생", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingHealthData = false,
                    healthSyncMessage = "오류 발생: ${e.message}"
                )
            }
        }
    }
}
