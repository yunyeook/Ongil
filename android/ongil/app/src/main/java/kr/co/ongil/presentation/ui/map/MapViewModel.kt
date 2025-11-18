package kr.co.ongil.presentation.ui.map

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
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
import kr.co.ongil.common.location.SafetyZoneStateManager

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
    private val stopSosAlertUseCase: StopSosAlertUseCase,
    val safetyZoneStateManager: SafetyZoneStateManager
) : ViewModel() {

    companion object {
        private const val BASE_URL = BuildConfig.BASE_URL
        private const val ARRIVAL_DISTANCE_METERS = 20.0
    }

    // 검색 관련
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchPlace>>(emptyList())
    val searchResults: StateFlow<List<SearchPlace>> = _searchResults.asStateFlow()

    private val _finalSearchResults = MutableStateFlow<List<SearchPlace>?>(null)
    val finalSearchResults: StateFlow<List<SearchPlace>?> = _finalSearchResults.asStateFlow()

    private val _selectedPlaceDetail = MutableStateFlow<kr.co.ongil.domain.model.PlaceDetail?>(null)
    val selectedPlaceDetail: StateFlow<kr.co.ongil.domain.model.PlaceDetail?> = _selectedPlaceDetail.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // 길안내 관련
    private val _navigationRoute = MutableStateFlow<Route?>(null)
    val navigationRoute: StateFlow<Route?> = _navigationRoute.asStateFlow()

    private var currentNavigationId: String? = null
    private var currentPatientId: Long? = null
    private var destinationLatitude: Double? = null
    private var destinationLongitude: Double? = null

    private val _showArrivalDialog = MutableStateFlow(false)
    val showArrivalDialog: StateFlow<Boolean> = _showArrivalDialog.asStateFlow()

    data class DestinationChangeRequest(
        val endLatitude: Double,
        val endLongitude: Double,
        val endName: String,
        val selectedPatientId: String?
    )
    private val _showDestinationChangeDialog = MutableStateFlow<DestinationChangeRequest?>(null)
    val showDestinationChangeDialog: StateFlow<DestinationChangeRequest?> = _showDestinationChangeDialog.asStateFlow()

    private val _isNavigationMode = MutableStateFlow(false)
    val isNavigationMode: StateFlow<Boolean> = _isNavigationMode.asStateFlow()

    private val _isNavigationModalVisible = MutableStateFlow(false)
    val isNavigationModalVisible: StateFlow<Boolean> = _isNavigationModalVisible.asStateFlow()

    // 기타
    private val _defaultDestination = MutableStateFlow<Pair<Double, Double>?>(null)
    val defaultDestination: StateFlow<Pair<Double, Double>?> = _defaultDestination.asStateFlow()

    private val _safeZoneSettings = MutableStateFlow(SafeZoneSettings())
    val safeZoneSettings: StateFlow<SafeZoneSettings> = _safeZoneSettings.asStateFlow()

    private val _isSosActive = MutableStateFlow(false)
    val isSosActive: StateFlow<Boolean> = _isSosActive.asStateFlow()

    private var patientLocationForSearch: kr.co.ongil.data.model.location.Coordinate? = null

    init {
        viewModelScope.launch { loadDefaultDestination() }
        observePatientChanges()
        observeSafeZoneSettings()
        observeSearchQuery()
        observeLocationForArrival()
        observeNavigationRoute()
    }

    // ============ 초기화 및 관찰 ============

    private fun observePatientChanges() {
        viewModelScope.launch {
            userDataStoreManager.getSelectedPatientId().collect { selectedId ->
                val userType = userDataStoreManager.getUserType().first()
                if (userType == "GUARDIAN" && selectedId != null) {
                    loadDefaultDestination()
                }
            }
        }
    }

    private fun observeSafeZoneSettings() {
        viewModelScope.launch {
            val userType = userDataStoreManager.getUserType().first()
            val patientId = if (userType == "PATIENT") {
                userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            } else {
                userDataStoreManager.getSelectedPatientId().first()?.toLongOrNull()
            }

            patientId?.let {
                userDataStoreManager.observeSafeZoneSettings(it).collect { settings ->
                    _safeZoneSettings.value = settings
                }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotBlank()) {
                        searchPlaces(query)
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeLocationForArrival() {
        viewModelScope.launch {
            locationBus.updates
                .debounce(3000)
                .collectLatest { location ->
                    checkArrival(location.latitude, location.longitude)
                }
        }
    }

    private fun observeNavigationRoute() {
        viewModelScope.launch {
            navigationRouteManager.currentRoute.collect { route ->
                _navigationRoute.value = route?.let {
                    Route(
                        path = it.path.map { latLng ->
                            kr.co.ongil.domain.model.LatLng(latLng.latitude, latLng.longitude)
                        },
                        totalTimeMinutes = it.totalTimeMinutes,
                        totalDistanceMeters = it.totalDistanceMeters
                    )
                }
            }
        }
    }

    // ============ 기본 목적지 ============

    fun refreshDefaultDestination() {
        viewModelScope.launch { loadDefaultDestination() }
    }

    private suspend fun loadDefaultDestination() {
        try {
            val userType = userDataStoreManager.getUserType().first()
            val patientId = if (userType == "PATIENT") {
                userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            } else {
                userDataStoreManager.getSelectedPatientId().first()?.toLongOrNull()
            }

            if (patientId == null) {
                Log.w("MapViewModel", "환자 ID를 가져올 수 없어 기본 목적지를 조회할 수 없습니다")
                return
            }

            val favoritePlaces = favoriteRepository.getFavoritePlaces(patientId)
            val defaultPlace = favoritePlaces.getOrNull()?.items?.firstOrNull { it.isDefault }

            if (defaultPlace == null) {
                _defaultDestination.value = null
                return
            }

            _defaultDestination.value = Pair(defaultPlace.latitude, defaultPlace.longitude)
            Log.d("MapViewModel", "기본 목적지: ${defaultPlace.displayName}")
        } catch (e: Exception) {
            Log.e("MapViewModel", "기본 목적지 조회 실패", e)
            _defaultDestination.value = null
        }
    }

    // ============ 검색 ============

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun updatePatientLocation(location: kr.co.ongil.data.model.location.Coordinate?) {
        patientLocationForSearch = location
    }

    private suspend fun searchPlaces(query: String) {
        _isSearching.value = true

        val userType = userDataStoreManager.getUserType().first()
        val (latitude, longitude) = if (userType == "GUARDIAN" && patientLocationForSearch != null) {
            patientLocationForSearch!!.latitude to patientLocationForSearch!!.longitude
        } else {
            locationBus.lastValue?.latitude to locationBus.lastValue?.longitude
        }

        searchPlaceUseCase(query, latitude, longitude)
            .onSuccess { _searchResults.value = it }
            .onFailure { _searchResults.value = emptyList() }

        _isSearching.value = false
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun refreshSearch() {
        viewModelScope.launch {
            val query = _searchQuery.value
            if (query.isNotBlank()) searchPlaces(query)
        }
    }

    fun onFinalSearch(radius: Int? = 3000) {
        viewModelScope.launch {
            val query = _searchQuery.value
            if (query.isBlank()) return@launch

            _searchResults.value = emptyList()

            val userType = userDataStoreManager.getUserType().first()
            val (latitude, longitude) = if (userType == "GUARDIAN" && patientLocationForSearch != null) {
                patientLocationForSearch!!.latitude to patientLocationForSearch!!.longitude
            } else {
                locationBus.lastValue?.latitude to locationBus.lastValue?.longitude
            }

            _isSearching.value = true
            mapRepository.searchPlaces(query, latitude, longitude, radius)
                .onSuccess { _finalSearchResults.value = it }
                .onFailure { _finalSearchResults.value = emptyList() }
            _isSearching.value = false
        }
    }

    fun closeFinalSearchResults() {
        _finalSearchResults.value = null
        refreshSearch()
    }

    // ============ 장소 상세 ============

    fun onPlaceClick(poiId: String) {
        viewModelScope.launch {
            val userType = userDataStoreManager.getUserType().first()
            val patientId = if (userType == "PATIENT") {
                userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            } else {
                userDataStoreManager.getSelectedPatientId().first()?.toLongOrNull()
            }

            mapRepository.getPlaceDetail(poiId)
                .onSuccess { placeDetail ->
                    if (patientId != null) {
                        favoriteRepository.getFavoritePlaces(patientId)
                            .onSuccess { favoritePlaces ->
                                val matchedFavorite = favoritePlaces.items.find { favorite ->
                                    val latDiff = kotlin.math.abs(favorite.latitude - placeDetail.latitude)
                                    val lonDiff = kotlin.math.abs(favorite.longitude - placeDetail.longitude)
                                    latDiff < 0.0001 && lonDiff < 0.0001
                                }
                                _selectedPlaceDetail.value = placeDetail.copy(
                                    isFavorite = matchedFavorite != null,
                                    favoriteId = matchedFavorite?.favoriteId
                                )
                            }
                            .onFailure { _selectedPlaceDetail.value = placeDetail }
                    } else {
                        _selectedPlaceDetail.value = placeDetail
                    }
                }
        }
    }

    fun closePlaceDetail() {
        _selectedPlaceDetail.value = null
    }

    fun toggleFavorite(onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val placeDetail = _selectedPlaceDetail.value
            if (placeDetail == null) {
                onError("장소 정보가 없습니다")
                return@launch
            }

            val userType = userDataStoreManager.getUserType().first()
            val patientId = if (userType == "PATIENT") {
                userDataStoreManager.getLoginUserId().first()?.toLongOrNull()
            } else {
                userDataStoreManager.getSelectedPatientId().first()?.toLongOrNull()
            }

            if (patientId == null) {
                onError("환자 ID를 찾을 수 없습니다")
                return@launch
            }

            if (placeDetail.isFavorite && placeDetail.favoriteId != null) {
                favoriteRepository.deleteFavoritePlace(patientId, placeDetail.favoriteId)
                    .onSuccess {
                        _selectedPlaceDetail.value = placeDetail.copy(isFavorite = false, favoriteId = null)
                        onSuccess(it)
                    }
                    .onFailure { onError("즐겨찾기 삭제에 실패했습니다") }
            } else {
                val address = placeDetail.roadAddress ?: placeDetail.jibunAddress ?: ""
                favoriteRepository.addFavoritePlace(
                    patientId, placeDetail.name, null, placeDetail.category,
                    address, placeDetail.latitude, placeDetail.longitude, false
                )
                    .onSuccess {
                        _selectedPlaceDetail.value = placeDetail.copy(isFavorite = true, favoriteId = it.favoriteId)
                        onSuccess("즐겨찾기에 추가되었습니다")
                    }
                    .onFailure { onError("즐겨찾기 추가에 실패했습니다") }
            }
        }
    }

    // ============ 전화 ============

    fun onClickCall(onNavigateToCall: (String, String, Long, String) -> Unit) {
        viewModelScope.launch {
            try {
                val userType = userDataStoreManager.getUserType().first() ?: "PATIENT"
                val relationshipsResult = favoriteRepository.getMyRelationships()
                val relationships = relationshipsResult.getOrNull()
                if (relationships.isNullOrEmpty()) return@launch

                if (userType == "PATIENT") {
                    val defaultGuardian = relationships.find { it.isDefault } ?: relationships.firstOrNull()
                    defaultGuardian?.let {
                        onNavigateToCall(it.name, it.phoneNumber, it.id, userType)
                    }
                } else {
                    val selectedPatientId = userDataStoreManager.getSelectedPatientId().first()
                    val selectedPatient = relationships.find { it.id.toString() == selectedPatientId }
                    selectedPatient?.let {
                        onNavigateToCall(it.name, it.phoneNumber, it.id, userType)
                    }
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "전화 걸기 실패", e)
            }
        }
    }

    fun logCall(phoneNumber: String) {
        viewModelScope.launch {
            val location = locationBus.lastValue ?: return@launch
            val currentTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

            mapRepository.createCallLog(
                receiverPhoneNumber = phoneNumber,
                callType = "NORMAL",
                source = "SYSTEM_DIALER",
                patientState = "NORMAL",
                latitude = location.latitude,
                longitude = location.longitude,
                startedAt = currentTime
            )
        }
    }

    // ============ 길안내 시작 ============

    fun startNavigation(
        endLatitude: Double,
        endLongitude: Double,
        endName: String,
        selectedPatientId: String? = null
    ) {
        if (_navigationRoute.value != null) {
            _showDestinationChangeDialog.value = DestinationChangeRequest(
                endLatitude, endLongitude, endName, selectedPatientId
            )
            return
        }
        startNavigationInternal(endLatitude, endLongitude, endName, selectedPatientId)
    }

    fun confirmDestinationChange() {
        val request = _showDestinationChangeDialog.value
        if (request != null) {
            _showDestinationChangeDialog.value = null
            viewModelScope.launch {
                currentNavigationId?.let { endNavigationApiOnly(it, false) }
                startNavigationInternal(
                    request.endLatitude,
                    request.endLongitude,
                    request.endName,
                    request.selectedPatientId
                )
            }
        }
    }

    fun cancelDestinationChange() {
        _showDestinationChangeDialog.value = null
    }

    private fun startNavigationInternal(
        endLatitude: Double,
        endLongitude: Double,
        endName: String,
        selectedPatientId: String? = null
    ) {
        viewModelScope.launch {
            val userType = userDataStoreManager.getUserType().first()
            val patientId = if (userType == "PATIENT") {
                userDataStoreManager.getLoginUserId().first()?.toLongOrNull() ?: 1L
            } else {
                selectedPatientId?.toLongOrNull() ?: 1L
            }

            val startLocation = if (userType == "GUARDIAN" && patientLocationForSearch != null) {
                patientLocationForSearch
            } else {
                locationBus.lastValue
            } ?: return@launch

            currentPatientId = patientId

            val (startLat, startLng) = when (startLocation) {
                is kr.co.ongil.data.model.location.Coordinate ->
                    startLocation.latitude to startLocation.longitude
                else -> {
                    val loc = startLocation as kr.co.ongil.common.location.LocationPoint
                    loc.latitude to loc.longitude
                }
            }

            val startAddress = getAddressFromCoordinates(startLat, startLng)

            findRouteUseCase(
                patientId, startLat, startLng, startAddress,
                endLatitude, endLongitude, endName,
                if (userType == "PATIENT") "PATIENT" else "GUARDIAN"
            ).onSuccess { result ->
                currentNavigationId = result.navigationId
                _navigationRoute.value = result.route
                destinationLatitude = endLatitude
                destinationLongitude = endLongitude

                gpsWebSocketManager.connect(BASE_URL)

                val routePath = result.route.path.map {
                    NavigationRouteManager.LatLng(it.latitude, it.longitude)
                }
                navigationRouteManager.setRoute(
                    result.navigationId, routePath, startAddress, endName,
                    result.route.totalTimeMinutes, result.route.totalDistanceMeters
                )

                _isNavigationMode.value = true //1인칭 시점 활성화
                _isNavigationModalVisible.value = true
            }
        }
    }

    // ============ 길안내 종료 ============

    fun stopNavigation() {
        viewModelScope.launch {
            // UI 즉시 초기화
            clearNavigationUI()

            val navigationIdString = navigationRouteManager.getCurrentRoute()?.navigationId
            if (navigationIdString == null) {
                navigationRouteManager.clearRoute()
                return@launch
            }

            val currentLocation = locationBus.lastValue
            val route = navigationRouteManager.getCurrentRoute()

            if (currentLocation == null || route == null || route.path.isEmpty()) {
                endNavigationApiOnly(navigationIdString, false)
                return@launch
            }

            val destination = route.path.last()
            val distance = calculateDistance(
                currentLocation.latitude, currentLocation.longitude,
                destination.latitude, destination.longitude
            )

            endNavigationApiOnly(navigationIdString, distance <= ARRIVAL_DISTANCE_METERS)
        }
    }

    fun confirmArrival() {
        viewModelScope.launch {
            _showArrivalDialog.value = false
            currentNavigationId?.let { endNavigationApiOnly(it, true) }
        }
    }

    fun dismissArrivalDialog() {
        _showArrivalDialog.value = false
    }

    private fun checkArrival(currentLat: Double, currentLng: Double) {
        val destLat = destinationLatitude
        val destLng = destinationLongitude
        if (destLat == null || destLng == null || _navigationRoute.value == null) return

        val distance = calculateDistance(currentLat, currentLng, destLat, destLng)
        if (distance <= ARRIVAL_DISTANCE_METERS && !_showArrivalDialog.value) {
            _showArrivalDialog.value = true
        }
    }

    private suspend fun endNavigationApiOnly(navigationIdString: String, isSuccessful: Boolean) {
        val patientId = currentPatientId ?: 1L
        val navigationId = navigationIdString.toIntOrNull()

        if (navigationId == null) {
            clearNavigationState()
            return
        }

        mapRepository.endNavigation(patientId, navigationId, isSuccessful)
        gpsWebSocketManager.disconnect()
        clearNavigationState()
    }

    private fun clearNavigationUI() {
        _navigationRoute.value = null
        _isNavigationMode.value = false
        _isNavigationModalVisible.value = false
    }

    private fun clearNavigationState() {
        navigationRouteManager.clearRoute()
        currentNavigationId = null
        currentPatientId = null
        destinationLatitude = null
        destinationLongitude = null
    }

    // ============ 모달 제어 ============

    fun closeNavigationModal() {
        _isNavigationModalVisible.value = false
    }

    fun showNavigationModal() {
        _isNavigationModalVisible.value = true
    }

    // ============ SSE 동기화 (환자용) ============

    fun syncNavigationFromSse(route: Route) {
        viewModelScope.launch {
            val navigationId = route.navigationId ?: "sse_${System.currentTimeMillis()}"
            currentNavigationId = navigationId

            val routePath = route.path.map {
                NavigationRouteManager.LatLng(it.latitude, it.longitude)
            }
            navigationRouteManager.setRoute(
                navigationId, routePath,
                route.startLocationName ?: "출발지",
                route.endLocationName ?: "목적지",
                route.totalTimeMinutes, route.totalDistanceMeters
            )

            val userType = userDataStoreManager.getUserType().first()
            _isNavigationMode.value = true
            _isNavigationModalVisible.value = true

            if (route.path.isNotEmpty()) {
                val destination = route.path.last()
                destinationLatitude = destination.latitude
                destinationLongitude = destination.longitude
            }
        }
    }

    fun clearNavigationFromSse() {
        viewModelScope.launch {
            clearNavigationUI()
            clearNavigationState()
        }
    }

    // ============ SOS ============

    fun sendSosAlert(patientId: Int) {
        viewModelScope.launch {
            sendSosAlertUseCase(patientId).onSuccess {
                _isSosActive.value = true
            }
        }
    }

    fun stopSosAlert(patientId: Int) {
        viewModelScope.launch {
            stopSosAlertUseCase(patientId).onSuccess {
                _isSosActive.value = false
            }
        }
    }

    // ============ 유틸리티 ============

    private suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.KOREA)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    var resultAddress = "내 위치"
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            resultAddress = when {
                                address.thoroughfare != null -> {
                                    if (address.subThoroughfare != null) {
                                        "${address.thoroughfare} ${address.subThoroughfare}"
                                    } else {
                                        address.thoroughfare!!
                                    }
                                }
                                else -> {
                                    val fullAddress = address.getAddressLine(0) ?: ""
                                    val parts = fullAddress.split(" ")
                                    if (parts.size >= 4) {
                                        "${parts[parts.size - 2]} ${parts[parts.size - 1]}"
                                    } else "내 위치"
                                }
                            }
                        }
                    }
                    kotlinx.coroutines.delay(500)
                    resultAddress
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        when {
                            address.thoroughfare != null -> {
                                if (address.subThoroughfare != null) {
                                    "${address.thoroughfare} ${address.subThoroughfare}"
                                } else {
                                    address.thoroughfare!!
                                }
                            }
                            else -> {
                                val fullAddress = address.getAddressLine(0) ?: ""
                                val parts = fullAddress.split(" ")
                                if (parts.size >= 4) {
                                    "${parts[parts.size - 2]} ${parts[parts.size - 1]}"
                                } else "내 위치"
                            }
                        }
                    } else "내 위치"
                }
            } catch (e: Exception) {
                "내 위치"
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    override fun onCleared() {
        super.onCleared()
        gpsWebSocketManager.disconnect()
    }
}