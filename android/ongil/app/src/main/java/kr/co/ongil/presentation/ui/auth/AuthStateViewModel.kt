package kr.co.ongil.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.model.location.Coordinate
import kr.co.ongil.data.model.location.SseEvent
import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.domain.model.Route
import kr.co.ongil.domain.model.LatLng as DomainLatLng
import kr.co.ongil.domain.repository.UserRepository
import kr.co.ongil.domain.repository.FavoriteRepository
import kr.co.ongil.domain.repository.LocationSseRepository
import kr.co.ongil.presentation.ui.favorite.PatientData
import javax.inject.Inject

/**
 * 앱 시작 시 인증 상태를 확인하는 ViewModel
 */
@HiltViewModel
class AuthStateViewModel @Inject constructor(
    private val userDataStoreManager: UserDataStoreManager,
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository,
    private val locationSseRepository: LocationSseRepository
) : ViewModel() {

//    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
//    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()
//
//    private val _currentUserId = MutableStateFlow<Int?>(null)
//    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()
//
//    init {
//        checkLoginState()
//    }

    val currentUserId: StateFlow<String?> = userDataStoreManager.getLoginUserId().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000), null
    )
    val isLoggedIn: StateFlow<Boolean?> = currentUserId.map { it != null }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000), null
    )

//    /**
//     * 저장된 토큰 확인하여 로그인 상태 체크
//     */
//    private fun checkLoginState() {
//        viewModelScope.launch {
//            val accessToken = tokenManager.getAccessToken().firstOrNull()
//            val loggedIn = !accessToken.isNullOrEmpty()
//            _isLoggedIn.value = loggedIn
//
//            if (loggedIn) {
//                loadUserInfo()
//            }
//        }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUserInfo: Flow<Result<UserDto>?> = currentUserId.flatMapLatest { userId ->
        if (userId != null) {
            userRepository.getMyInfo()
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val patientList: StateFlow<List<PatientData>> = currentUserId.flatMapLatest { userId ->
        if (userId != null) {
            flow {
                val result = favoriteRepository.getMyRelationships()
                emit(result.getOrNull() ?: emptyList())
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedPatientId: StateFlow<String?> = userDataStoreManager.getSelectedPatientId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // 환자들의 실시간 위치 (Map<patientId, Coordinate>)
    private val _patientLocations = MutableStateFlow<Map<Long, Coordinate>>(emptyMap())
    val patientLocations: StateFlow<Map<Long, Coordinate>> = _patientLocations.asStateFlow()

    // 환자들의 길찾기 경로 (Map<patientId, Route?>)
    private val _patientNavigationRoutes = MutableStateFlow<Map<Long, Route?>>(emptyMap())
    val patientNavigationRoutes: StateFlow<Map<Long, Route?>> =
        _patientNavigationRoutes.asStateFlow()

    /**
     * 환자 선택 시 DataStore에 저장
     */
    fun selectPatient(patientId: String) {
        viewModelScope.launch {
            userDataStoreManager.saveSelectedPatientId(patientId)
        }
    }

    /**
     * SSE 연결 시작 (보호자일 때만)
     */
    private var sseConnectionJob: kotlinx.coroutines.Job? = null
    private var retryCount = 0
    private val maxRetryDelay = 30000L // 최대 30초

    private fun startSseConnection() {
        // 기존 연결이 있으면 취소
        sseConnectionJob?.cancel()

        sseConnectionJob = viewModelScope.launch {
            while (true) {  // 무한 재연결 루프
                try {
                    // android.util.Log.d("AuthStateViewModel", "SSE 연결 시작 중... (재시도 ${retryCount}회)")
                    locationSseRepository.subscribeSseEvents().collect { event ->
                        // 연결 성공 시 재시도 카운트 리셋
                        retryCount = 0

                        when (event) {
                            is SseEvent.Connected -> {
                                //android.util.Log.d("AuthStateViewModel", "✅ SSE 연결 완료")
                            }

                            is SseEvent.GpsUpdate -> {
                                val gpsUpdate = event.data
//                                android.util.Log.d(
//                                    "AuthStateViewModel",
//                                    "GPS 업데이트 수신: patientId=${gpsUpdate.patientId}, lat=${gpsUpdate.coordinate.latitude}, lon=${gpsUpdate.coordinate.longitude}"
//                                )
                                _patientLocations.update { currentMap ->
                                    val updated =
                                        currentMap + (gpsUpdate.patientId to gpsUpdate.coordinate)
//                                    android.util.Log.d(
//                                        "AuthStateViewModel",
//                                        "patientLocations 업데이트: ${updated.keys}"
//                                    )
                                    updated
                                }
                            }

                            is SseEvent.NavigationUpdate -> {
                                val navUpdate = event.data
                                android.util.Log.d(
                                    "AuthStateViewModel",
                                    "길찾기 업데이트 수신: patientId=${navUpdate.patientId}, status=${navUpdate.status}"
                                )

                                when (navUpdate.status) {
                                    "STARTED" -> {
                                        // 경로 데이터를 Domain Route로 변환
                                        navUpdate.route?.let { sseRoute ->
                                            val domainRoute = Route(
                                                totalTimeMinutes = (sseRoute.totalTime + 59) / 60,
                                                totalDistanceMeters = sseRoute.totalDistance,
                                                path = sseRoute.path.map { coord ->
                                                    DomainLatLng(
                                                        latitude = coord.latitude,
                                                        longitude = coord.longitude
                                                    )
                                                },
                                                navigationId = sseRoute.navigationId,
                                                startLocationName = sseRoute.startLocation.name,
                                                endLocationName = sseRoute.endLocation.name
                                            )

                                            _patientNavigationRoutes.update { currentMap ->
                                                currentMap + (navUpdate.patientId to domainRoute)
                                            }
                                            android.util.Log.d(
                                                "AuthStateViewModel",
                                                "환자 ${navUpdate.patientId} 길찾기 시작: ${sseRoute.startLocation.name} → ${sseRoute.endLocation.name} (${sseRoute.path.size}개 포인트)"
                                            )
                                        }
                                    }

                                    "ENDED" -> {
                                        _patientNavigationRoutes.update { currentMap ->
                                            currentMap + (navUpdate.patientId to null)
                                        }
                                        android.util.Log.d(
                                            "AuthStateViewModel",
                                            "환자 ${navUpdate.patientId} 길찾기 종료"
                                        )
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    retryCount++
                    // 지수 백오프 계산 (1초 → 2초 → 4초 → 8초 → ... → 최대 30초)
                    val delay = kotlin.math.min(
                        1000L * (1 shl (retryCount - 1)),  // 2^(n-1) 초
                        maxRetryDelay
                    )

//                    android.util.Log.e(
//                        "AuthStateViewModel",
//                        "⚠️ SSE 연결 중 오류 (재시도 ${retryCount}회, ${delay/1000}초 후 재연결)",
//                        e
//                    )

                    // 재연결 대기
                    kotlinx.coroutines.delay(delay)
                }
            }
        }
    }

    init {
        // 사용자 타입에 따라 SSE 연결 (보호자와 환자 모두)
        viewModelScope.launch {
            currentUserInfo.collect { userInfoResult ->
                val userType = userInfoResult?.getOrNull()?.userType
                android.util.Log.d("AuthStateViewModel", "사용자 타입: $userType")
                if (userType == "GUARDIAN" || userType == "PATIENT") {
                    android.util.Log.d("AuthStateViewModel", "$userType 로그인 감지 - SSE 연결 시작")
                    startSseConnection()
                }
            }
        }

        // 기본 환자 자동 선택 (보호자일 때만)
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                patientList,
                selectedPatientId,
                currentUserInfo
            ) { patients, selectedId, userInfo ->
                Triple(patients, selectedId, userInfo?.getOrNull()?.userType)
            }.collect { (patients, selectedId, userType) ->
                // 보호자이고, 선택된 환자가 없고, 환자 목록이 있을 때
                if (userType == "GUARDIAN" && selectedId == null && patients.isNotEmpty()) {
                    // isDefault=true인 환자 찾기
                    val defaultPatient = patients.find { it.isDefault }
                    if (defaultPatient != null) {
                        android.util.Log.d(
                            "AuthStateViewModel",
                            "기본 환자 자동 선택: ${defaultPatient.name} (id=${defaultPatient.id})"
                        )
                        selectPatient(defaultPatient.id.toString())
                    } else {
                        // isDefault가 없으면 첫 번째 환자 선택
                        val firstPatient = patients.firstOrNull()
                        if (firstPatient != null) {
                            android.util.Log.d(
                                "AuthStateViewModel",
                                "첫 번째 환자 자동 선택: ${firstPatient.name} (id=${firstPatient.id})"
                            )
                            selectPatient(firstPatient.id.toString())
                        }
                    }
                }
            }
        }
    }
}