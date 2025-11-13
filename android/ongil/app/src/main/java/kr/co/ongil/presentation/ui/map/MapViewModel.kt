package kr.co.ongil.presentation.ui.map

import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.vector.path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.co.ongil.domain.model.SearchPlace
import kr.co.ongil.domain.repository.MapRepository
import kr.co.ongil.domain.usecase.map.SearchPlaceUseCase
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.domain.model.Route
import kr.co.ongil.domain.usecase.map.FindRouteUseCase
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings
import javax.inject.Inject
import kr.co.ongil.BuildConfig
import kr.co.ongil.common.location.NavigationRouteManager
import kr.co.ongil.data.websocket.GpsWebSocketManager
import kr.co.ongil.domain.usecase.sosalert.SendSosAlertUseCase
import kr.co.ongil.domain.usecase.sosalert.StopSosAlertUseCase

/**
 * 지도 화면 ViewModel
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    val locationBus: LocationStreamBus,
    private val searchPlaceUseCase: SearchPlaceUseCase,
    private val mapRepository: MapRepository,
    private val userDataStoreManager: UserDataStoreManager,
    private val findRouteUseCase: FindRouteUseCase,
    private val navigationRouteManager: NavigationRouteManager,
    private val gpsWebSocketManager: GpsWebSocketManager,
    private val sendSosAlertUseCase: SendSosAlertUseCase,
    private val stopSosAlertUseCase: StopSosAlertUseCase
) : ViewModel() {

    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
    }

    // 검색어
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 검색 결과 (실시간 TMap 검색)
    private val _searchResults = MutableStateFlow<List<SearchPlace>>(emptyList())
    val searchResults: StateFlow<List<SearchPlace>> = _searchResults.asStateFlow()

    // 최종 검색 결과 (백엔드 API 검색)
    private val _finalSearchResults = MutableStateFlow<List<SearchPlace>?>(null)
    val finalSearchResults: StateFlow<List<SearchPlace>?> = _finalSearchResults.asStateFlow()

    // 선택된 장소 상세 정보
    private val _selectedPlaceDetail = MutableStateFlow<kr.co.ongil.domain.model.PlaceDetail?>(null)
    val selectedPlaceDetail: StateFlow<kr.co.ongil.domain.model.PlaceDetail?> = _selectedPlaceDetail.asStateFlow()

    // 길안내 정보
    private val _navigationRoute = MutableStateFlow<Route?>(null)
    val navigationRoute: StateFlow<Route?> = _navigationRoute.asStateFlow()

    // 길안내 ID (종료 시 필요, API 응답은 String)
    private var currentNavigationId: String? = null

    // 길안내 중인 환자 ID (종료 시 필요)
    private var currentPatientId: Long? = null

    // 목적지 좌표 (도착 감지용)
    private var destinationLatitude: Double? = null
    private var destinationLongitude: Double? = null

    // 도착 확인 모달 표시 상태
    private val _showArrivalDialog = MutableStateFlow(false)
    val showArrivalDialog: StateFlow<Boolean> = _showArrivalDialog.asStateFlow()

    // 네비게이션 모드 (1인칭 시점)
    private val _isNavigationMode = MutableStateFlow(false)
    val isNavigationMode: StateFlow<Boolean> = _isNavigationMode.asStateFlow()

    // 로딩 상태
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // 안전구역 설정값
    private val _safeZoneSettings = MutableStateFlow(SafeZoneSettings())
    val safeZoneSettings: StateFlow<SafeZoneSettings> = _safeZoneSettings.asStateFlow()

    // SOS 알림 활성화 상태
    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    init {
        // 안전구역 설정값 구독
        viewModelScope.launch {
            userDataStoreManager.observeSafeZoneSettings().collect { settings ->
                _safeZoneSettings.value = settings
            }
        }
        // 검색어 변경 시 debounce 적용하여 자동 검색
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _searchQuery
                .debounce(500) // 500ms 대기
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotBlank()) {
                        searchPlaces(query)
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }

        // 위치 변경 감지하여 목적지 도착 여부 체크 (3초마다)
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            locationBus.updates
                .debounce(3000) // 3초마다 체크
                .collectLatest { location ->
                    checkArrival(location.latitude, location.longitude)
                }
        }
    }

    /**
     * 두 좌표 간의 거리 계산 (Haversine formula, 단위: 미터)
     */
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // 지구 반지름 (미터)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    /**
     * 목적지 도착 여부 체크 (20m 이내)
     */
    private fun checkArrival(currentLat: Double, currentLng: Double) {
        val destLat = destinationLatitude
        val destLng = destinationLongitude

        // 길안내 중이 아니거나 목적지 좌표가 없으면 체크하지 않음
        if (destLat == null || destLng == null || _navigationRoute.value == null) {
            return
        }

        val distance = calculateDistance(currentLat, currentLng, destLat, destLng)
        Log.d("MapViewModel", "목적지까지 거리: ${distance.toInt()}m")

        // 20m 이내 접근 시 도착 모달 표시 (한 번만)
        if (distance <= 20.0 && !_showArrivalDialog.value) {
            Log.d("MapViewModel", "목적지 도착! 모달 표시")
            _showArrivalDialog.value = true
        }
    }

    /**
     * 검색어 업데이트
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * 장소 검색 (실시간 TMap 검색)
     */
    private suspend fun searchPlaces(query: String) {
        _isSearching.value = true

        val location = locationBus.lastValue
        Log.d("MapViewModel", "장소 검색: $query (lat: ${location?.latitude}, lng: ${location?.longitude})")

        searchPlaceUseCase(
            query = query,
            latitude = location?.latitude,
            longitude = location?.longitude
        )
            .onSuccess { places ->
                _searchResults.value = places
                Log.d("MapViewModel", "검색 성공: ${places.size}개")
            }
            .onFailure { e ->
                Log.e("MapViewModel", "검색 실패: ${e.message}", e)
                _searchResults.value = emptyList()
            }

        _isSearching.value = false
    }

    /**
     * 검색 결과 초기화
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    /**
     * 최종 검색 (백엔드 API 호출)
     * 검색 버튼을 눌렀을 때 호출
     */
    fun onFinalSearch(radius: Int? = 3000) {
        Log.d("MapViewModel", "onFinalSearch 호출됨")
        viewModelScope.launch {
            val query = _searchQuery.value
            Log.d("MapViewModel", "검색어: $query")
            if (query.isBlank()) {
                Log.d("MapViewModel", "검색어가 비어있음")
                return@launch
            }

            val location = locationBus.lastValue
            val latitude = location?.latitude
            val longitude = location?.longitude

            _isSearching.value = true
            Log.d("MapViewModel", "최종 검색 시작: $query (lat: $latitude, lng: $longitude)")

            mapRepository.searchPlaces(
                query = query,
                latitude = latitude,
                longitude = longitude,
                radius = radius
            )
                .onSuccess { places ->
                    _finalSearchResults.value = places
                    Log.d("MapViewModel", "최종 검색 성공: ${places.size}개")
                }
                .onFailure { e ->
                    Log.e("MapViewModel", "최종 검색 실패: ${e.message}", e)
                    _finalSearchResults.value = emptyList()
                }

            _isSearching.value = false
        }
    }

    /**
     * 최종 검색 결과 모달 닫기
     */
    fun closeFinalSearchResults() {
        _finalSearchResults.value = null
    }

    /**
     * 전화 걸기 버튼 클릭
     * TODO: 전화 기능 구현 필요
     */
    fun onClickCall() {
        Log.d("MapViewModel", "전화 걸기 버튼 클릭됨 (기능 미구현)")
        // TODO: 전화 걸기 기능 구현
    }

    /**
     * 장소 상세 정보 조회
     */
    fun onPlaceClick(poiId: String) {
        viewModelScope.launch {
            Log.d("MapViewModel", "장소 상세 조회 시작: $poiId")

            mapRepository.getPlaceDetail(poiId)
                .onSuccess { placeDetail ->
                    _selectedPlaceDetail.value = placeDetail
                    Log.d("MapViewModel", "장소 상세 조회 성공: ${placeDetail.name}")
                }
                .onFailure { e ->
                    Log.e("MapViewModel", "장소 상세 조회 실패: ${e.message}", e)
                }
        }
    }

    /**
     * 장소 상세 정보 모달 닫기
     */
    fun closePlaceDetail() {
        _selectedPlaceDetail.value = null
    }

    /**
     * 통화 로그 기록
     */
    fun logCall(phoneNumber: String) {
        viewModelScope.launch {
            val location = locationBus.lastValue
            if (location == null) {
                Log.e("MapViewModel", "통화 로그 기록 실패: 위치 정보 없음")
                return@launch
            }

            Log.d("MapViewModel", "통화 로그 기록 시작: $phoneNumber")

            // ISO 8601 형식으로 현재 시간 포맷
            val currentTime = java.time.ZonedDateTime.now()
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            mapRepository.createCallLog(
                receiverPhoneNumber = phoneNumber,
                callType = "OUTGOING",
                source = "MAP",
                patientState = "NORMAL",
                latitude = location.latitude,
                longitude = location.longitude,
                startedAt = currentTime
            )
                .onSuccess {
                    Log.d("MapViewModel", "통화 로그 기록 성공")
                }
                .onFailure { e ->
                    Log.e("MapViewModel", "통화 로그 기록 실패: ${e.message}", e)
                }
        }
    }

    /**
     * 길안내 시작
     */
    fun startNavigation(
        endLatitude: Double,
        endLongitude: Double,
        endName: String,
        selectedPatientId: String? = null
    ) {
        viewModelScope.launch {
            val location = locationBus.lastValue
            if (location == null) {
                Log.e("MapViewModel", "길안내 시작 실패: 위치 정보 없음")
                return@launch
            }

            Log.d("MapViewModel", "길안내 시작: $endName")

            // userType에 따라 patientId 결정
            val userType = userDataStoreManager.getUserType().first()
            val patientId = if (userType == "PATIENT") {
                // 환자: 자기 자신의 ID
                userDataStoreManager.getLoginUserId().first()?.toLongOrNull() ?: 1L
            } else {
                // 보호자: 선택된 환자 ID
                selectedPatientId?.toLongOrNull() ?: 1L
            }

            Log.d("MapViewModel", "userType=$userType, patientId=$patientId")

            // 길안내 종료 시 사용하기 위해 저장
            currentPatientId = patientId

            findRouteUseCase(
                patientId = patientId,
                startLatitude = location.latitude,
                startLongitude = location.longitude,
                startName = "내 위치",
                endLatitude = endLatitude,
                endLongitude = endLongitude,
                endName = endName,
                initiatedBy = "PATIENT"
            ).onSuccess { result ->
                Log.d("MapViewModel", "경로 탐색 성공: navigationId=${result.navigationId}, 경로 포인트 수=${result.route.path.size}")
                currentNavigationId = result.navigationId
                _navigationRoute.value = result.route

                // 목적지 좌표 저장 (도착 감지용)
                destinationLatitude = endLatitude
                destinationLongitude = endLongitude

                // GPS WebSocket 연결 시작
                Log.d("MapViewModel", "GPS WebSocket 연결 시작")
                gpsWebSocketManager.connect(BASE_URL)

                // 경로 정보를 NavigationRouteManager에 저장
                val routePath = result.route.path.map { latLng ->
                    NavigationRouteManager.LatLng(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    )
                }
                navigationRouteManager.setRoute(result.navigationId, routePath)
                Log.d("MapViewModel", "경로 정보 저장 완료: ${routePath.size}개 포인트")

                // 네비게이션 모드 활성화 (환자일 때만)
                val userType = userDataStoreManager.getUserType().first()
                if (userType == "PATIENT") {
                    _isNavigationMode.value = true
                    Log.d("MapViewModel", "네비게이션 모드 활성화 (환자)")
                } else {
                    Log.d("MapViewModel", "보호자는 네비게이션 모드 비활성화")
                }

            }.onFailure { e ->
                Log.e("MapViewModel", "경로 탐색 실패: ${e.message}", e)
            }
        }
    }

    /**
     * 길안내 종료 (수동 종료 - 안내중지 버튼)
     */
    fun stopNavigation() {
        viewModelScope.launch {
            val navigationIdString = currentNavigationId
            if (navigationIdString == null) {
                Log.e("MapViewModel", "길안내 종료 실패: navigationId 없음")
                _navigationRoute.value = null
                return@launch
            }

            // 현재 위치 확인
            val currentLocation = locationBus.lastValue
            if (currentLocation == null) {
                Log.e("MapViewModel", "길안내 종료 실패: 위치 정보 없음")
                return@launch
            }

            // 목적지 좌표 확인
            val destLat = destinationLatitude
            val destLng = destinationLongitude
            if (destLat == null || destLng == null) {
                Log.e("MapViewModel", "길안내 종료 실패: 목적지 정보 없음")
                return@launch
            }

            // 현재 위치와 목적지 거리 계산
            val distance = calculateDistance(
                currentLocation.latitude,
                currentLocation.longitude,
                destLat,
                destLng
            )

            // 20m 이내면 성공, 아니면 실패
            val isSuccessful = distance <= 20.0
            Log.d("MapViewModel", "길안내 수동 종료: 거리=${distance.toInt()}m, isSuccessful=$isSuccessful")

            endNavigationApi(navigationIdString, isSuccessful)
        }
    }

    /**
     * 길안내 종료 (자동 도착 확인)
     */
    fun confirmArrival() {
        viewModelScope.launch {
            val navigationIdString = currentNavigationId
            if (navigationIdString == null) {
                Log.e("MapViewModel", "길안내 종료 실패: navigationId 없음")
                return@launch
            }

            Log.d("MapViewModel", "목적지 도착 확인됨, 길안내 종료")
            _showArrivalDialog.value = false
            endNavigationApi(navigationIdString, isSuccessful = true)
        }
    }

    /**
     * 도착 확인 모달 취소
     */
    fun dismissArrivalDialog() {
        _showArrivalDialog.value = false
    }

    /**
     * 길안내 종료 API 호출 (공통)
     */
    private suspend fun endNavigationApi(navigationIdString: String, isSuccessful: Boolean) {
        val patientId = currentPatientId ?: 1L

        Log.d("MapViewModel", "길안내 종료 API 호출: navigationId=$navigationIdString, isSuccessful=$isSuccessful")

        // String -> Int 변환
        val navigationId = navigationIdString.toIntOrNull()
        if (navigationId == null) {
            Log.e("MapViewModel", "길안내 종료 실패: navigationId 변환 오류")
            _navigationRoute.value = null
            currentNavigationId = null
            currentPatientId = null
            destinationLatitude = null
            destinationLongitude = null
            return
        }

        mapRepository.endNavigation(
            patientId = patientId,
            navigationId = navigationId,
            isSuccessful = isSuccessful
        ).onSuccess { response ->
            Log.d("MapViewModel", "길안내 종료 성공: ${response.message}")
        }.onFailure { e ->
            Log.e("MapViewModel", "길안내 종료 실패: ${e.message}", e)
        }

        // GPS WebSocket 연결 해제 (성공/실패 관계없이 항상 해제)
        Log.d("MapViewModel", "GPS WebSocket 연결 해제")
        gpsWebSocketManager.disconnect()

        // 경로 정보 초기화
        navigationRouteManager.clearRoute()
        Log.d("MapViewModel", "경로 정보 초기화 완료")

        _isNavigationMode.value = false
        Log.d("MapViewModel", "네비게이션 모드 비활성화")

        // 상태 초기화
        _navigationRoute.value = null
        currentNavigationId = null
        currentPatientId = null
        destinationLatitude = null
        destinationLongitude = null
    }

    // SOS 알림 시작
    fun sendSosAlert(patientId: Int) {
        viewModelScope.launch {
            sendSosAlertUseCase(patientId).onSuccess {
                Log.d("MapViewModel", "SOS 알림 시작 성공")
                _isSosActive.value = true
            }.onFailure { error ->
                Log.e("MapViewModel", "SOS 알림 시작 실패: ${error.message}")
            }
        }
    }

    // SOS 알림 종료
    fun stopSosAlert(patientId: Int) {
        viewModelScope.launch {
            stopSosAlertUseCase(patientId).onSuccess {
                Log.d("MapViewModel", "SOS 알림 종료 성공")
                _isSosActive.value = false
            }.onFailure { error ->
                Log.e("MapViewModel", "SOS 알림 종료 실패: ${error.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel 정리 시 WebSocket 연결 해제
        gpsWebSocketManager.disconnect()
    }
}