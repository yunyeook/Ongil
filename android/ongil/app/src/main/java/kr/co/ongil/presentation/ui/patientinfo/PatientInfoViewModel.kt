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

    // 건강 데이터 로드 + 자동 서버 동기화 (중복 방지)
    private fun loadHealthData() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "loadHealthData() - 건강 데이터 로드 시작")

                getHealthDataUseCase().collectLatest { result ->
                    result.onSuccess { localHealthData ->
                        Log.d(TAG, "loadHealthData() - 건강 데이터 조회 성공")
                        _uiState.value = _uiState.value.copy(healthData = localHealthData)

                        // 🔁 자동 서버 동기화 (중복 방지)
                        val pid = currentPatientId
                        if (pid != null) {
                            launch {
                                syncHealthDataWithDuplicateCheck(pid, localHealthData)
                            }
                        }
                    }.onFailure { exception ->
                        Log.e(TAG, "loadHealthData() - 건강 데이터 조회 실패", exception)
                        _uiState.value = _uiState.value.copy(healthData = null)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadHealthData() - 예외 발생", e)
                _uiState.value = _uiState.value.copy(healthData = null)
            }
        }
    }

    /**
     * 수동 건강 데이터 동기화 (버튼 클릭 시 호출)
     */
    fun syncHealthDataManually() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoadingHealthData = true,
                    healthSyncMessage = null
                )

                // 1. Health Connect에서 최신 데이터 가져오기
                val result = getHealthDataUseCase().first()
                result.onSuccess { localHealthData ->
                    Log.d(TAG, "syncHealthDataManually() - 로컬 데이터 조회 성공")

                    // 2. 서버에 동기화 (중복 체크 포함)
                    val pid = currentPatientId
                    if (pid != null) {
                        syncHealthDataWithDuplicateCheck(pid, localHealthData)
                        _uiState.value = _uiState.value.copy(
                            healthData = localHealthData,
                            isLoadingHealthData = false,
                            healthSyncMessage = "건강 데이터를 동기화했습니다"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoadingHealthData = false,
                            healthSyncMessage = "환자 ID를 찾을 수 없습니다"
                        )
                    }
                }.onFailure { e ->
                    Log.e(TAG, "syncHealthDataManually() - 로컬 데이터 조회 실패", e)
                    _uiState.value = _uiState.value.copy(
                        isLoadingHealthData = false,
                        healthSyncMessage = "건강 데이터 조회 실패: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "syncHealthDataManually() - 예외 발생", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingHealthData = false,
                    healthSyncMessage = "동기화 실패: ${e.message}"
                )
            }
        }
    }

    /**
     * 중복 체크를 통한 건강 데이터 동기화
     * 서버에 이미 존재하는 데이터는 제외하고 새로운 데이터만 업로드
     */
    private suspend fun syncHealthDataWithDuplicateCheck(
        patientId: Long,
        localHealthData: kr.co.ongil.data.model.health.LocalHealthData
    ) {
        try {
            // 1. 서버에서 기존 데이터 조회 (최근 30일)
            Log.d(TAG, "syncHealthDataWithDuplicateCheck() - 서버 데이터 조회 시작")

            val serverDataResult = getHealthDataFromServerUseCase(
                patientId = patientId,
                type = null, // 전체 타입 조회
                from = null, // 기본값: 1일 전
                to = null,   // 기본값: 현재
                sort = "measuredAt,desc"
            )

            serverDataResult.onSuccess { serverResponse ->
                Log.d(TAG, "syncHealthDataWithDuplicateCheck() - 서버 데이터 ${serverResponse.data.records.size}개 조회 완료")

                // 2. 서버에 있는 measuredAt 시간 목록 추출
                val serverMeasuredTimes = serverResponse.data.records.map { it.measuredAt }.toSet()
                Log.d(TAG, "syncHealthDataWithDuplicateCheck() - 서버에 저장된 시간: $serverMeasuredTimes")

                // 3. 로컬 데이터를 도메인 모델로 변환
                val domainData = localHealthData.toDomain()

                // 4. 중복 제거: 서버에 없는 데이터만 필터링
                val newHeartRateRecords = domainData.heartRateRecords.filter {
                    it.measuredAt !in serverMeasuredTimes
                }
                val newOxygenRecords = domainData.oxygenSaturationRecords.filter {
                    it.measuredAt !in serverMeasuredTimes
                }
                val newSleepRecords = domainData.sleepRecords.filter {
                    it.measuredAt !in serverMeasuredTimes
                }
                val newStepsRecords = domainData.stepsRecords.filter {
                    it.measuredAt !in serverMeasuredTimes
                }

                val totalNewRecords = newHeartRateRecords.size + newOxygenRecords.size +
                        newSleepRecords.size + newStepsRecords.size

                Log.d(TAG, "syncHealthDataWithDuplicateCheck() - 새로운 데이터: 심박수=${newHeartRateRecords.size}, 산소=${newOxygenRecords.size}, 수면=${newSleepRecords.size}, 걸음수=${newStepsRecords.size}")

                // 5. 새로운 데이터가 있으면 배치로 업로드 (500개씩)
                if (totalNewRecords > 0) {
                    val batchSize = 500
                    var uploadedCount = 0

                    // 타입별로 배치 분할 및 업로드
                    val heartRateBatches = newHeartRateRecords.chunked(batchSize)
                    val oxygenBatches = newOxygenRecords.chunked(batchSize)
                    val sleepBatches = newSleepRecords.chunked(batchSize)
                    val stepsBatches = newStepsRecords.chunked(batchSize)

                    val maxBatches = maxOf(
                        heartRateBatches.size,
                        oxygenBatches.size,
                        sleepBatches.size,
                        stepsBatches.size
                    )

                    for (i in 0 until maxBatches) {
                        val batchData = kr.co.ongil.domain.model.HealthData(
                            heartRateRecords = heartRateBatches.getOrNull(i) ?: emptyList(),
                            oxygenSaturationRecords = oxygenBatches.getOrNull(i) ?: emptyList(),
                            sleepRecords = sleepBatches.getOrNull(i) ?: emptyList(),
                            stepsRecords = stepsBatches.getOrNull(i) ?: emptyList()
                        )

                        val batchCount = batchData.heartRateRecords.size +
                                batchData.oxygenSaturationRecords.size +
                                batchData.sleepRecords.size +
                                batchData.stepsRecords.size

                        if (batchCount > 0) {
                            uploadHealthDataUseCase(patientId, batchData)
                                .onSuccess { message ->
                                    uploadedCount += batchCount
                                    Log.d(TAG, "HealthData 배치 ${i + 1}/${maxBatches} 업로드 성공: ${batchCount}개 (총 ${uploadedCount}/${totalNewRecords})")
                                }
                                .onFailure { e ->
                                    Log.e(TAG, "HealthData 배치 ${i + 1} 업로드 실패", e)
                                }
                        }
                    }

                    Log.d(TAG, "syncHealthDataWithDuplicateCheck() - 전체 업로드 완료: ${uploadedCount}개 레코드")
                } else {
                    Log.d(TAG, "syncHealthDataWithDuplicateCheck() - 업로드할 새로운 데이터가 없습니다")
                }
            }.onFailure { e ->
                Log.w(TAG, "syncHealthDataWithDuplicateCheck() - 서버 데이터 조회 실패, 배치 업로드 시도", e)

                // 서버 조회 실패 시 전체 데이터를 배치로 업로드 (중복 가능성 있음)
                val domainData = localHealthData.toDomain()
                val batchSize = 500

                val heartRateBatches = domainData.heartRateRecords.chunked(batchSize)
                val oxygenBatches = domainData.oxygenSaturationRecords.chunked(batchSize)
                val sleepBatches = domainData.sleepRecords.chunked(batchSize)
                val stepsBatches = domainData.stepsRecords.chunked(batchSize)

                val maxBatches = maxOf(
                    heartRateBatches.size,
                    oxygenBatches.size,
                    sleepBatches.size,
                    stepsBatches.size
                )

                var uploadedCount = 0
                val totalRecords = domainData.heartRateRecords.size +
                        domainData.oxygenSaturationRecords.size +
                        domainData.sleepRecords.size +
                        domainData.stepsRecords.size

                for (i in 0 until maxBatches) {
                    val batchData = kr.co.ongil.domain.model.HealthData(
                        heartRateRecords = heartRateBatches.getOrNull(i) ?: emptyList(),
                        oxygenSaturationRecords = oxygenBatches.getOrNull(i) ?: emptyList(),
                        sleepRecords = sleepBatches.getOrNull(i) ?: emptyList(),
                        stepsRecords = stepsBatches.getOrNull(i) ?: emptyList()
                    )

                    val batchCount = batchData.heartRateRecords.size +
                            batchData.oxygenSaturationRecords.size +
                            batchData.sleepRecords.size +
                            batchData.stepsRecords.size

                    if (batchCount > 0) {
                        uploadHealthDataUseCase(patientId, batchData)
                            .onSuccess { message ->
                                uploadedCount += batchCount
                                Log.d(TAG, "HealthData 배치 ${i + 1}/${maxBatches} 업로드 성공: ${batchCount}개 (총 ${uploadedCount}/${totalRecords})")
                            }
                            .onFailure { uploadError ->
                                Log.e(TAG, "HealthData 배치 ${i + 1} 업로드 실패", uploadError)
                            }
                    }
                }

                Log.d(TAG, "HealthData 전체 배치 업로드 완료: ${uploadedCount}개 레코드")
            }
        } catch (e: Exception) {
            Log.e(TAG, "syncHealthDataWithDuplicateCheck() - 예외 발생", e)
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
                        // 환자: Health Connect 권한 체크 및 로컬 데이터 로드
                        checkHealthPermissions()

                        userDataStoreManager.getLoginUserId().firstOrNull()?.let { patientIdStr ->
                            if (patientIdStr.isNotEmpty()) {
                                loadInsightData(patientIdStr)
                            }
                        }
                    } else {
                        // 보호자: 서버에서 건강 데이터 로드
                        userDataStoreManager.getSelectedPatientId().firstOrNull()?.let { patientIdStr ->
                            if (patientIdStr.isNotEmpty()) {
                                loadInsightData(patientIdStr)
                                loadHealthDataFromServer()
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

            try {
                Log.d(TAG, "syncHealthDataToServer() - Health Connect에서 데이터 조회 시작")

                // Health Connect에서 최신 데이터 조회
                getHealthDataUseCase().collect { result ->
                    result.onSuccess { localHealthData ->
                        Log.d(TAG, "syncHealthDataToServer() - 조회 성공: 심박수=${localHealthData.heartRateRecords.size}개")

                        // 데이터가 비어있는지 확인
                        val hasData = localHealthData.heartRateRecords.isNotEmpty() ||
                                     localHealthData.oxygenSaturationRecords.isNotEmpty() ||
                                     localHealthData.sleepRecords.isNotEmpty() ||
                                     localHealthData.stepsRecords.isNotEmpty()

                        if (!hasData) {
                            Log.w(TAG, "syncHealthDataToServer() - 건강 데이터가 없습니다")
                            _uiState.value = _uiState.value.copy(
                                error = "Samsung Health에 건강 데이터가 없습니다."
                            )
                            return@collect
                        }

                        // UI 업데이트
                        _uiState.value = _uiState.value.copy(healthData = localHealthData)

                        // LocalHealthData → 도메인 모델로 변환
                        val domainData = localHealthData.toDomain()

                        // 서버에 업로드
                        uploadHealthDataUseCase(pid, domainData)
                            .onSuccess { message ->
                                Log.d(TAG, "수동 동기화 성공: $message")
                                _uiState.value = _uiState.value.copy(
                                    error = null
                                )
                            }
                            .onFailure { e ->
                                Log.e(TAG, "수동 동기화 실패", e)
                                _uiState.value = _uiState.value.copy(
                                    error = "건강 데이터 동기화에 실패했습니다."
                                )
                            }
                    }.onFailure { exception ->
                        Log.e(TAG, "syncHealthDataToServer() - Health Connect 조회 실패", exception)
                        _uiState.value = _uiState.value.copy(
                            error = "건강 데이터 조회에 실패했습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "syncHealthDataToServer() - 예외 발생", e)
                _uiState.value = _uiState.value.copy(
                    error = "오류가 발생했습니다: ${e.message}"
                )
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
     * 서버에서 건강 데이터 로드 (보호자용)
     * 최근 30일 데이터를 가져와서 평균/최대/최소 계산
     */
    private fun loadHealthDataFromServer() {
        viewModelScope.launch {
            val pid = currentPatientId
            if (pid == null) {
                Log.w(TAG, "loadHealthDataFromServer() - 환자 ID가 없습니다")
                return@launch
            }

            try {
                _uiState.value = _uiState.value.copy(isLoadingHealthData = true)
                Log.d(TAG, "loadHealthDataFromServer() - 서버에서 건강 데이터 조회 시작 (patientId: $pid)")

                // 최근 30일 데이터를 타입별로 가져오기
                val today = java.time.LocalDate.now()
                val from = today.minusDays(30).format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
                val to = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))

                // 각 타입별로 요약 데이터 조회
                val heartRateStat = getHealthStatFromServer(pid, HealthDataType.HEART_RATE, from, to)
                val oxygenStat = getHealthStatFromServer(pid, HealthDataType.OXYGEN_SATURATION, from, to)
                val sleepStat = getHealthStatFromServer(pid, HealthDataType.SLEEP, from, to)
                val stepsStat = getHealthStatFromServer(pid, HealthDataType.STEP_COUNT, from, to)

                val serverHealthData = ServerHealthData(
                    heartRate = heartRateStat,
                    oxygenSaturation = oxygenStat,
                    sleep = sleepStat,
                    steps = stepsStat
                )

                _uiState.value = _uiState.value.copy(
                    serverHealthData = serverHealthData,
                    isLoadingHealthData = false
                )

                Log.d(TAG, "loadHealthDataFromServer() - 서버 건강 데이터 로드 완료")
            } catch (e: Exception) {
                Log.e(TAG, "loadHealthDataFromServer() - 예외 발생", e)
                _uiState.value = _uiState.value.copy(
                    isLoadingHealthData = false,
                    error = "건강 데이터를 불러오는데 실패했습니다: ${e.message}"
                )
            }
        }
    }

    /**
     * 특정 타입의 건강 데이터 통계를 서버에서 가져오기
     */
    private suspend fun getHealthStatFromServer(
        patientId: Long,
        type: HealthDataType,
        from: String,
        to: String
    ): HealthStat? {
        return try {
            val result = getHealthDataSummaryUseCase(
                patientId = patientId,
                type = type,
                from = from,
                to = to
            )

            result.getOrNull()?.let { response ->
                val summaries = response.data.summary
                if (summaries.isEmpty()) {
                    Log.d(TAG, "getHealthStatFromServer() - ${type.name} 데이터 없음")
                    return null
                }

                // 전체 기간의 평균/최대/최소 계산
                val allAverages = summaries.map { it.average }
                val allMaxes = summaries.map { it.max }
                val allMins = summaries.map { it.min }

                val overallAverage = allAverages.average()
                val overallMax = allMaxes.maxOrNull() ?: 0.0
                val overallMin = allMins.minOrNull() ?: 0.0

                Log.d(TAG, "getHealthStatFromServer() - ${type.name}: avg=$overallAverage, max=$overallMax, min=$overallMin")

                HealthStat(
                    average = overallAverage,
                    max = overallMax,
                    min = overallMin,
                    unit = response.data.unit ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "getHealthStatFromServer() - ${type.name} 조회 실패", e)
            null
        }
    }
}
