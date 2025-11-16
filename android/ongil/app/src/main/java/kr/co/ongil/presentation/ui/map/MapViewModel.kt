package kr.co.ongil.presentation.ui.map

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.vector.path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.ongil.domain.model.SearchPlace
import kr.co.ongil.domain.repository.MapRepository
import kr.co.ongil.domain.usecase.map.SearchPlaceUseCase
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.domain.model.Route
import kr.co.ongil.domain.usecase.map.FindRouteUseCase
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettings
import java.util.Locale
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
    @ApplicationContext private val context: Context,
    val locationBus: LocationStreamBus,
    private val searchPlaceUseCase: SearchPlaceUseCase,
    private val mapRepository: MapRepository,
    private val userDataStoreManager: UserDataStoreManager,
    private val findRouteUseCase: FindRouteUseCase,
    private val navigationRouteManager: NavigationRouteManager,
    private val gpsWebSocketManager: GpsWebSocketManager,
    private val favoriteRepository: kr.co.ongil.domain.repository.FavoriteRepository,
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

    // 목적지 변경 확인 모달 상태
    data class DestinationChangeRequest(
        val endLatitude: Double,
        val endLongitude: Double,
        val endName: String,
        val selectedPatientId: String?
    )
    private val _showDestinationChangeDialog = MutableStateFlow<DestinationChangeRequest?>(null)
    val showDestinationChangeDialog: StateFlow<DestinationChangeRequest?> = _showDestinationChangeDialog.asStateFlow()

    // 네비게이션 모드 (1인칭 시점)
    private val _isNavigationMode = MutableStateFlow(false)
    val isNavigationMode: StateFlow<Boolean> = _isNavigationMode.asStateFlow()

    // 길찾기 모달 표시 상태
    private val _isNavigationModalVisible = MutableStateFlow(false)
    val isNavigationModalVisible: StateFlow<Boolean> = _isNavigationModalVisible.asStateFlow()

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

        // 길찾기 경로 변경 감지 (화면 복귀 시 모달 자동 표시)
        viewModelScope.launch {
            navigationRouteManager.currentRoute.collect { route ->
                if (route != null) {
                    _navigationRoute.value = Route(
                        path = route.path.map { latLng ->
                            kr.co.ongil.domain.model.LatLng(
                                latitude = latLng.latitude,
                                longitude = latLng.longitude
                            )
                        },
                        totalTimeMinutes = route.totalTimeMinutes,
                        totalDistanceMeters = route.totalDistanceMeters
                    )
                    // 길찾기가 시작되면 모달 표시
                    if (!_isNavigationModalVisible.value) {
                        _isNavigationModalVisible.value = true
                        Log.d("MapViewModel", "길찾기 경로 감지 - 모달 자동 표시")
                    }
                }
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
     * 실시간 검색 결과만 숨김 (검색어는 유지)
     */
    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    /**
     * 현재 검색어로 검색 다시 실행
     */
    fun refreshSearch() {
        viewModelScope.launch {
            val query = _searchQuery.value
            if (query.isNotBlank()) {
                searchPlaces(query)
            }
        }
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

            // 실시간 검색 결과 숨김
            _searchResults.value = emptyList()

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
        // 실시간 검색 결과 다시 표시
        viewModelScope.launch {
            val query = _searchQuery.value
            if (query.isNotBlank()) {
                searchPlaces(query)
            }
        }
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

            // 현재 환자 ID 가져오기
            val userType = userDataStoreManager.getUserType().first()
            val patientId = if (userType == "PATIENT") {
                userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            } else {
                userDataStoreManager.getSelectedPatientId().first()?.toLongOrNull()
            }

            // 장소 상세 정보 조회
            mapRepository.getPlaceDetail(poiId)
                .onSuccess { placeDetail ->
                    // 즐겨찾기 목록 조회하여 현재 장소가 포함되어 있는지 확인
                    if (patientId != null) {
                        favoriteRepository.getFavoritePlaces(patientId)
                            .onSuccess { favoritePlaces ->
                                // 좌표로 즐겨찾기 목록에 있는지 확인 (위도/경도 비교)
                                val matchedFavorite = favoritePlaces.items.find { favorite ->
                                    val latDiff = kotlin.math.abs(favorite.latitude - placeDetail.latitude)
                                    val lonDiff = kotlin.math.abs(favorite.longitude - placeDetail.longitude)
                                    // 좌표 차이가 0.0001도 이내면 같은 장소로 간주 (약 11m)
                                    latDiff < 0.0001 && lonDiff < 0.0001
                                }
                                val isFavorite = matchedFavorite != null

                                // isFavorite 플래그 설정
                                val updatedPlaceDetail = placeDetail.copy(
                                    isFavorite = isFavorite,
                                    favoriteId = matchedFavorite?.favoriteId
                                )
                                _selectedPlaceDetail.value = updatedPlaceDetail
                                Log.d("MapViewModel", "장소 상세 조회 성공: ${placeDetail.name}, isFavorite: $isFavorite, favoriteId: ${matchedFavorite?.favoriteId}")
                            }
                            .onFailure { e ->
                                Log.e("MapViewModel", "즐겨찾기 목록 조회 실패: ${e.message}", e)
                                // 즐겨찾기 조회 실패 시에도 장소 상세 정보는 표시
                                _selectedPlaceDetail.value = placeDetail
                            }
                    } else {
                        // patientId가 없으면 즐겨찾기 확인 없이 표시
                        _selectedPlaceDetail.value = placeDetail
                        Log.d("MapViewModel", "장소 상세 조회 성공: ${placeDetail.name} (patientId 없음)")
                    }
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
     * 좌표를 주소로 변환 (Reverse Geocoding)
     * 도로명 주소만 반환 (예: "테헤란로 427")
     */
    private suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.KOREA)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13 이상: 비동기 API 사용
                    var resultAddress = "내 위치"
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            Log.d("MapViewModel", "주소 정보: thoroughfare=${address.thoroughfare}, subThoroughfare=${address.subThoroughfare}, featureName=${address.featureName}, addressLine=${address.getAddressLine(0)}")

                            // 도로명 주소 추출
                            resultAddress = when {
                                // 1. thoroughfare + subThoroughfare (도로명 + 번지)
                                address.thoroughfare != null -> {
                                    if (address.subThoroughfare != null) {
                                        "${address.thoroughfare} ${address.subThoroughfare}"
                                    } else {
                                        address.thoroughfare!!
                                    }
                                }
                                // 2. getAddressLine에서 도로명 주소 부분만 추출
                                else -> {
                                    val fullAddress = address.getAddressLine(0) ?: ""
                                    // "서울특별시 강남구 테헤란로 427" 형태에서 "테헤란로 427"만 추출
                                    val parts = fullAddress.split(" ")
                                    if (parts.size >= 4) {
                                        "${parts[parts.size - 2]} ${parts[parts.size - 1]}"
                                    } else {
                                        "내 위치"
                                    }
                                }
                            }
                        }
                    }
                    // 비동기 결과를 기다리는 간단한 방법
                    kotlinx.coroutines.delay(500)
                    resultAddress
                } else {
                    // Android 12 이하: 동기 API 사용
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        Log.d("MapViewModel", "주소 정보: thoroughfare=${address.thoroughfare}, subThoroughfare=${address.subThoroughfare}, featureName=${address.featureName}, addressLine=${address.getAddressLine(0)}")

                        // 도로명 주소 추출
                        when {
                            // 1. thoroughfare + subThoroughfare (도로명 + 번지)
                            address.thoroughfare != null -> {
                                if (address.subThoroughfare != null) {
                                    "${address.thoroughfare} ${address.subThoroughfare}"
                                } else {
                                    address.thoroughfare!!
                                }
                            }
                            // 2. getAddressLine에서 도로명 주소 부분만 추출
                            else -> {
                                val fullAddress = address.getAddressLine(0) ?: ""
                                // "서울특별시 강남구 테헤란로 427" 형태에서 "테헤란로 427"만 추출
                                val parts = fullAddress.split(" ")
                                if (parts.size >= 4) {
                                    "${parts[parts.size - 2]} ${parts[parts.size - 1]}"
                                } else {
                                    "내 위치"
                                }
                            }
                        }
                    } else {
                        "내 위치"
                    }
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "주소 변환 실패: ${e.message}", e)
                "내 위치"
            }
        }
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

            // 백엔드가 요구하는 형식: yyyy-MM-dd'T'HH:mm:ss (타임존 제외)
            val currentTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

            mapRepository.createCallLog(
                receiverPhoneNumber = phoneNumber,
                callType = "NORMAL",  // 백엔드 CallType enum: NORMAL, EMERGENCY
                source = "SYSTEM_DIALER",  // 백엔드 CallSource enum: APP, SYSTEM_DIALER
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
     * 길안내 시작 요청 (길찾기 중이면 확인 모달 표시)
     */
    fun startNavigation(
        endLatitude: Double,
        endLongitude: Double,
        endName: String,
        selectedPatientId: String? = null
    ) {
        // 이미 길찾기 중이면 목적지 변경 확인 모달 표시
        if (_navigationRoute.value != null) {
            _showDestinationChangeDialog.value = DestinationChangeRequest(
                endLatitude = endLatitude,
                endLongitude = endLongitude,
                endName = endName,
                selectedPatientId = selectedPatientId
            )
            Log.d("MapViewModel", "길찾기 중 - 목적지 변경 확인 모달 표시")
            return
        }

        // 길찾기 중이 아니면 바로 시작
        startNavigationInternal(endLatitude, endLongitude, endName, selectedPatientId)
    }

    /**
     * 목적지 변경 확인
     */
    fun confirmDestinationChange() {
        val request = _showDestinationChangeDialog.value
        if (request != null) {
            _showDestinationChangeDialog.value = null
            // 기존 길찾기 종료 후 새로운 길찾기 시작
            viewModelScope.launch {
                // 기존 길찾기 종료
                val navigationIdString = currentNavigationId
                if (navigationIdString != null) {
                    endNavigationApi(navigationIdString, isSuccessful = false)
                }
                // 새로운 길찾기 시작
                startNavigationInternal(
                    request.endLatitude,
                    request.endLongitude,
                    request.endName,
                    request.selectedPatientId
                )
            }
        }
    }

    /**
     * 목적지 변경 취소
     */
    fun cancelDestinationChange() {
        _showDestinationChangeDialog.value = null
        Log.d("MapViewModel", "목적지 변경 취소")
    }

    /**
     * 길안내 시작 (내부 함수)
     */
    private fun startNavigationInternal(
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

            // 출발지 주소 가져오기
            val startAddress = getAddressFromCoordinates(location.latitude, location.longitude)
            Log.d("MapViewModel", "출발지 주소: $startAddress")

            findRouteUseCase(
                patientId = patientId,
                startLatitude = location.latitude,
                startLongitude = location.longitude,
                startName = startAddress,
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
                navigationRouteManager.setRoute(
                    navigationId = result.navigationId,
                    path = routePath,
                    startLocationName = startAddress,
                    endLocationName = endName,
                    totalTimeMinutes = result.route.totalTimeMinutes,
                    totalDistanceMeters = result.route.totalDistanceMeters
                )
                Log.d("MapViewModel", "경로 정보 저장 완료: ${routePath.size}개 포인트, 소요시간: ${result.route.totalTimeMinutes}분")

                // 네비게이션 모드 활성화 (환자일 때만)
                val userType = userDataStoreManager.getUserType().first()
                if (userType == "PATIENT") {
                    _isNavigationMode.value = true
                    Log.d("MapViewModel", "네비게이션 모드 활성화 (환자)")
                } else {
                    Log.d("MapViewModel", "보호자는 네비게이션 모드 비활성화")
                }

                // 길찾기 모달 표시
                _isNavigationModalVisible.value = true
                Log.d("MapViewModel", "길찾기 모달 표시")

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
     * 길찾기 모달 닫기 (다른 UI 표시)
     */
    fun closeNavigationModal() {
        _isNavigationModalVisible.value = false
        Log.d("MapViewModel", "길찾기 모달 닫기")
    }

    /**
     * 길찾기 모달 다시 표시 (다른 UI 숨김)
     */
    fun showNavigationModal() {
        _isNavigationModalVisible.value = true
        Log.d("MapViewModel", "길찾기 모달 표시")
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

        // 길찾기 모달 닫기
        _isNavigationModalVisible.value = false
        Log.d("MapViewModel", "길찾기 모달 닫기")

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

    /**
     * 즐겨찾기 토글 (추가/삭제)
     */
    fun toggleFavorite(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val placeDetail = _selectedPlaceDetail.value
            if (placeDetail == null) {
                onError("장소 정보가 없습니다")
                return@launch
            }

            // patientId 가져오기
            val userType = userDataStoreManager.getUserType().first()
            val patientId = if (userType == "PATIENT") {
                userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            } else {
                null
            }

            if (patientId == null) {
                onError("환자 ID를 찾을 수 없습니다")
                return@launch
            }

            if (placeDetail.isFavorite && placeDetail.favoriteId != null) {
                // 즐겨찾기 삭제
                Log.d("MapViewModel", "즐겨찾기 삭제 시작: favoriteId=${placeDetail.favoriteId}")
                favoriteRepository.deleteFavoritePlace(patientId, placeDetail.favoriteId)
                    .onSuccess { message ->
                        Log.d("MapViewModel", "즐겨찾기 삭제 성공: $message")
                        _selectedPlaceDetail.value = placeDetail.copy(
                            isFavorite = false,
                            favoriteId = null
                        )
                        onSuccess(message)
                    }
                    .onFailure { e ->
                        Log.e("MapViewModel", "즐겨찾기 삭제 실패: ${e.message}", e)
                        onError("즐겨찾기 삭제에 실패했습니다")
                    }
            } else {
                // 즐겨찾기 추가
                val address = placeDetail.roadAddress ?: placeDetail.jibunAddress ?: ""
                Log.d("MapViewModel", "즐겨찾기 추가 시작: placeName=${placeDetail.name}")
                favoriteRepository.addFavoritePlace(
                    patientId = patientId,
                    placeName = placeDetail.name,
                    placeAlias = null,
                    category = placeDetail.category,
                    address = address,
                    latitude = placeDetail.latitude,
                    longitude = placeDetail.longitude,
                    isDefault = false
                )
                    .onSuccess { favorite ->
                        Log.d("MapViewModel", "즐겨찾기 추가 성공: favoriteId=${favorite.favoriteId}")
                        _selectedPlaceDetail.value = placeDetail.copy(
                            isFavorite = true,
                            favoriteId = favorite.favoriteId
                        )
                        onSuccess("즐겨찾기에 추가되었습니다")
                    }
                    .onFailure { e ->
                        Log.e("MapViewModel", "즐겨찾기 추가 실패: ${e.message}", e)
                        onError("즐겨찾기 추가에 실패했습니다")
                    }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModel 정리 시 WebSocket 연결 해제
        gpsWebSocketManager.disconnect()
    }
}