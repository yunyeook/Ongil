package kr.co.ongil.presentation.ui.map

import androidx.compose.foundation.background
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kr.co.ongil.data.model.location.Coordinate
import kr.co.ongil.domain.model.Route
import kr.co.ongil.presentation.ui.auth.AuthStateViewModel
import kr.co.ongil.presentation.ui.common.map.CircleFloatingButton
import kr.co.ongil.presentation.ui.common.map.SearchBar
import kr.co.ongil.presentation.ui.common.map.SearchListItem
import kr.co.ongil.presentation.ui.safezonesetting.SafeZoneSettingRoutes
import kr.co.ongil.service.location.LocationTrackingService

/**
 * 지도 화면
 * - TMap 표시
 * - 장소 검색 (실시간)
 * - 도움요청 토글 버튼 (플로팅 버튼)
 */
@Composable
fun MapScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    viewModel: MapViewModel = hiltViewModel(),
    authViewModel: AuthStateViewModel,
    searchPlaceholder: String = "장소를 검색해주세요",
    requestSearchFocus: Boolean = false,
    onShowBarsChange: (Boolean) -> Unit = {},  // true: 표시, false: 숨김
    onNavigateToCall: (targetName: String, targetPhone: String, targetId: Long, userType: String) -> Unit = { _, _, _, _ -> }
) {
    // ViewModel 상태
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val finalSearchResults by viewModel.finalSearchResults.collectAsState()
    val selectedPlaceDetail by viewModel.selectedPlaceDetail.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val safeZoneSettings by viewModel.safeZoneSettings.collectAsState()
    val navigationRoute by viewModel.navigationRoute.collectAsState()
    val isNavigating = navigationRoute != null
    val showArrivalDialog by viewModel.showArrivalDialog.collectAsState()
    val showDestinationChangeDialog by viewModel.showDestinationChangeDialog.collectAsState()
    val isNavigationMode by viewModel.isNavigationMode.collectAsState()
    val isNavigationModalVisible by viewModel.isNavigationModalVisible.collectAsState()
    val defaultDestination by viewModel.defaultDestination.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    // 화면이 다시 보일 때마다 기본 목적지 새로고침 (즐겨찾기에서 기본 목적지 변경 시 반영)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                android.util.Log.d("MapScreen", "화면 재개 - 기본 목적지 새로고침")
                viewModel.refreshDefaultDestination()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // 경로 변경 감지 로그
    LaunchedEffect(navigationRoute) {
        android.util.Log.d("MapScreen", "navigationRoute 변경됨: ${navigationRoute != null}, 포인트 수: ${navigationRoute?.path?.size}")
    }

    // 사용자 정보
    val currentUserInfo by authViewModel.currentUserInfo.collectAsState(initial = null)
    val userType = currentUserInfo?.getOrNull()?.userType ?: ""
    val selectedPatientId by authViewModel.selectedPatientId.collectAsState()

    // 환자 위치 정보 (보호자용)
    val patientLocations by authViewModel.patientLocations.collectAsState()

    // 환자 길찾기 경로 (보호자용 - SSE로 받음)
    val patientNavigationRoutes by authViewModel.patientNavigationRoutes.collectAsState()

    // 실제 표시할 경로 (환자: 본인 경로, 보호자: 선택된 환자 경로 또는 본인이 시작한 경로)
    val displayRoute = if (userType == "GUARDIAN") {
        // 보호자: SSE로 받은 환자 경로가 우선, 없으면 보호자가 직접 시작한 경로 표시
        selectedPatientId?.toLongOrNull()?.let { id ->
            patientNavigationRoutes[id]
        } ?: navigationRoute
    } else {
        // 환자: 본인 경로
        navigationRoute
    }

    // 환자 위치 변경 모니터링 및 ViewModel 업데이트 (보호자용 검색)
    LaunchedEffect(patientLocations, selectedPatientId, userType) {
        android.util.Log.d("MapScreen", "patientLocations 변경: ${patientLocations.keys}, userType: $userType, selectedPatientId: $selectedPatientId")

        // 보호자인 경우 선택된 환자의 위치를 ViewModel에 전달
        if (userType == "GUARDIAN") {
            selectedPatientId?.toLongOrNull()?.let { patientId ->
                val patientLocation = patientLocations[patientId]
                android.util.Log.d("MapScreen", "보호자 검색용 환자 위치 업데이트: patientId=$patientId, location=$patientLocation")
                viewModel.updatePatientLocation(patientLocation)
            } ?: run {
                android.util.Log.d("MapScreen", "선택된 환자 없음 - 환자 위치 null로 설정")
                viewModel.updatePatientLocation(null)
            }
        }
    }

    // 환자 경로 변경 모니터링 및 NavigationRouteManager 동기화
    LaunchedEffect(patientNavigationRoutes, userType, currentUserInfo) {
        android.util.Log.d("MapScreen", "patientNavigationRoutes 변경: ${patientNavigationRoutes.keys}, userType: $userType")

        // 환자인 경우, SSE로 받은 자신의 경로를 NavigationRouteManager에 동기화
        if (userType == "PATIENT") {
            val myPatientId = currentUserInfo?.getOrNull()?.id?.toLong()
            if (myPatientId != null) {
                val myRoute = patientNavigationRoutes[myPatientId]
                android.util.Log.d("MapScreen", "환자 본인 경로 업데이트: patientId=$myPatientId, route=${myRoute != null}")

                if (myRoute != null) {
                    // SSE로 받은 경로를 NavigationRouteManager에 저장
                    // TODO: navigationId, 출발/도착지 이름을 SSE 데이터에 포함시켜야 함
                    // 현재는 임시로 빈 값 사용
                    viewModel.syncNavigationFromSse(myRoute)
                } else {
                    // 경로가 null이면 길찾기 종료된 것
                    viewModel.clearNavigationFromSse()
                }
            }
        }
    }

    // SOS 상태 (ViewModel과 동기화)
    val isSosActiveFromViewModel by viewModel.isSosActive.collectAsState()
    var isSosEnabled by remember { mutableStateOf(isSosActiveFromViewModel) }

    // ViewModel의 SOS 상태와 동기화
    LaunchedEffect(isSosActiveFromViewModel) {
        isSosEnabled = isSosActiveFromViewModel
    }

    // 안전범위 표시 토글 상태 (전역 상태 사용)
    val showSafetyZones by viewModel.safetyZoneStateManager.showSafetyZones.collectAsState()

    // 장소 상세 정보 표시 중 (검색창, 플로팅 버튼 숨김)
    // 길찾기 중이더라도 선택한 장소가 현재 목적지와 다르면 장소 상세 표시
    val isShowingPlaceDetail = selectedPlaceDetail != null && (
        !isNavigating || run {
            // 길찾기 중일 때: 선택한 장소와 현재 목적지의 좌표가 다른지 확인
            val detail = selectedPlaceDetail
            val route = navigationRoute
            if (detail != null && route != null && route.path.isNotEmpty()) {
                val destination = route.path.last()
                val latDiff = kotlin.math.abs(detail.latitude - destination.latitude)
                val lonDiff = kotlin.math.abs(detail.longitude - destination.longitude)
                // 좌표 차이가 0.0001도 이상이면 다른 장소로 간주 (약 11m)
                latDiff > 0.0001 || lonDiff > 0.0001
            } else {
                false
            }
        }
    )

    // 검색창 포커스 상태
    var isSearchFocused by remember { mutableStateOf(false) }

    // 검색창 포커스 요청 트리거
    var requestSearchFocusTrigger by remember { mutableStateOf(false) }

    // 검색 중 (검색창에 포커스가 있거나, 검색 결과가 있을 때)
    val isSearchActive = isSearchFocused || searchResults.isNotEmpty()

    // 키보드 높이 감지
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val imeHeightDp = with(density) { imeInsets.getBottom(density).toDp() }

    // 장소 위치로 이동 트리거
    var placeLocationTrigger by remember { mutableStateOf(0 to Pair(0.0, 0.0)) }

    // BottomSheet가 닫힐 때 검색창에 포커스
    LaunchedEffect(finalSearchResults) {
        if (finalSearchResults == null && searchQuery.isNotEmpty()) {
            requestSearchFocusTrigger = true
        }
    }

    // 장소 상세 정보 표시 시 해당 장소로 지도 이동
    LaunchedEffect(selectedPlaceDetail, isShowingPlaceDetail) {
        val detail = selectedPlaceDetail  // 로컬 변수로 복사
        if (detail != null && isShowingPlaceDetail) {
            // 장소 상세 정보 표시 시 마커 추가 (길찾기 중이어도 다른 장소 클릭하면 이동)
            placeLocationTrigger = (placeLocationTrigger.first + 1) to Pair(
                detail.latitude,
                detail.longitude
            )
            android.util.Log.d("MapScreen", "장소 상세로 지도 이동: ${detail.name} (${detail.latitude}, ${detail.longitude})")
        } else if (detail == null && isNavigating) {
            // 장소 상세 정보가 닫히고 길찾기 중이면 마커 제거 (경로로 복귀)
            placeLocationTrigger = (placeLocationTrigger.first + 1) to Pair(0.0, 0.0)
            android.util.Log.d("MapScreen", "장소 상세 닫힘 - 길찾기 경로로 복귀")
        } else if (detail == null && !isNavigating) {
            // 길찾기 중이 아닐 때 장소 상세가 닫히면 마커 제거
            placeLocationTrigger = (placeLocationTrigger.first + 1) to Pair(0.0, 0.0)
        }
    }

    // 장소 상세 정보 또는 길찾기 모달 표시 여부에 따라 헤더/하단바 숨김/표시
    LaunchedEffect(isShowingPlaceDetail, isNavigationModalVisible) {
        onShowBarsChange(!isShowingPlaceDetail && !isNavigationModalVisible)  // 장소 상세 또는 길찾기 모달 표시 중이면 false (숨김)
    }

    // 뒤로가기 버튼 처리
    // 즐겨찾기에서 길찾기 시작 요청 감지
    LaunchedEffect(navController.currentBackStackEntry) {
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle

        savedStateHandle?.getStateFlow("start_navigation", false)?.collect { startNavigation ->
            if (startNavigation) {
                val endLat = savedStateHandle.get<Double>("navigation_end_lat")
                val endLon = savedStateHandle.get<Double>("navigation_end_lon")
                val endName = savedStateHandle.get<String>("navigation_end_name")

                if (endLat != null && endLon != null && endName != null) {
                    android.util.Log.d("MapScreen", "🗺️ 즐겨찾기에서 길찾기 시작: $endName ($endLat, $endLon)")

                    // 길찾기 시작
                    viewModel.startNavigation(
                        endLatitude = endLat,
                        endLongitude = endLon,
                        endName = endName,
                        selectedPatientId = selectedPatientId
                    )

                    // savedStateHandle 정리
                    savedStateHandle.set("start_navigation", false)
                    savedStateHandle.remove<Double>("navigation_end_lat")
                    savedStateHandle.remove<Double>("navigation_end_lon")
                    savedStateHandle.remove<String>("navigation_end_name")

                    android.util.Log.d("MapScreen", "✅ 길찾기 시작 완료 및 savedStateHandle 정리")
                }
            }
        }
    }

    // 뒤로가기 버튼 처리 (장소 상세 정보 표시 중일 때)
    val focusManager = LocalFocusManager.current

    // 길찾기 모달 표시 중일 때 뒤로가기 -> 모달만 닫기 (길찾기는 계속 진행)
    BackHandler(enabled = isNavigationModalVisible) {
        viewModel.closeNavigationModal()
        android.util.Log.d("MapScreen", "길찾기 모달 닫기 (뒤로가기)")
    }

    // 검색 결과 표시 중일 때 뒤로가기 (키보드가 내려진 상태)
    BackHandler(enabled = isSearchActive && !isSearchFocused) {
        viewModel.clearSearchResults()
    }

    // 장소 상세 정보 표시 중일 때 뒤로가기
    BackHandler(enabled = isShowingPlaceDetail) {
        viewModel.closePlaceDetail()
        viewModel.clearSearch()  // 검색 상태 초기화
        focusManager.clearFocus()  // 키보드 숨김
        isSearchFocused = false  // 검색 포커스 상태 초기화
    }

    Box(modifier) {
        // 내 위치 버튼 클릭 트리거
        var myLocationTrigger by remember { mutableStateOf(0) }

        // 북쪽 고정 버튼 클릭 트리거
        var northUpTrigger by remember { mutableStateOf(0) }

        val context = LocalContext.current
        val activity = context as? Activity
        val inPreview = LocalInspectionMode.current

        // 위치 권한 상태
        var hasLocationPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        // 백그라운드 위치 권한 상태 (Android 10+)
        var hasBackgroundLocationPermission by remember {
            mutableStateOf(
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true  // Android 10 미만은 백그라운드 권한 불필요
                }
            )
        }

        // 백그라운드 권한 요청 여부
        var shouldRequestBackgroundPermission by remember { mutableStateOf(false) }

        // 위치 권한 요청 런처
        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            hasLocationPermission = granted

            // 위치 권한이 승인되면 백그라운드 권한 요청 (Android 10+, 환자만)
            if (granted && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
                && userType == "PATIENT" && !hasBackgroundLocationPermission) {
                shouldRequestBackgroundPermission = true
            }
        }

        // 백그라운드 위치 권한 요청 런처 (Android 10+)
        val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasBackgroundLocationPermission = isGranted
            if (isGranted) {
                android.util.Log.d("MapScreen", "✅ 백그라운드 위치 권한 승인됨")
            } else {
                android.util.Log.w("MapScreen", "⚠️ 백그라운드 위치 권한 거부됨 - 앱이 백그라운드에서 위치를 추적할 수 없습니다")
            }
        }

        // 일반 위치 권한 요청
        LaunchedEffect(Unit) {
            if (!inPreview && !hasLocationPermission) {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        // 백그라운드 위치 권한 요청 (일반 위치 권한 승인 후)
        LaunchedEffect(shouldRequestBackgroundPermission) {
            if (shouldRequestBackgroundPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.util.Log.d("MapScreen", "📍 백그라운드 위치 권한 요청 중...")
                backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                shouldRequestBackgroundPermission = false
            }
        }

        // 위치 추적 서비스 시작 (환자만, 권한이 있을 때)
        LaunchedEffect(hasLocationPermission, userType) {
            if (!inPreview && hasLocationPermission && userType == "PATIENT") {
                val intent = Intent(context, LocationTrackingService::class.java)
                    .setAction(LocationTrackingService.ACTION_START)
                ContextCompat.startForegroundService(context, intent)
            }
        }

        // 화면 종료 시 위치 추적 서비스 중지 (환자만)
        DisposableEffect(userType) {
            onDispose {
                if (!inPreview && userType == "PATIENT") {
                    val stop = Intent(context, LocationTrackingService::class.java)
                        .setAction(LocationTrackingService.ACTION_STOP)
                    context.startService(stop)
                }
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            // TMap 표시
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = 0.dp
                    )
            ) {
                TMapComposable(
                    modifier = Modifier.fillMaxSize(),
                    locationBus = if (inPreview) null else viewModel.locationBus,
                    enableTracking = !inPreview,
                    myLocationTrigger = myLocationTrigger,
                    route = displayRoute,  // 환자는 본인 경로, 보호자는 선택된 환자의 경로
                    northUpTrigger = northUpTrigger,
                    isNavigationMode = isNavigationMode,
                    userType = userType,
                    selectedPatientId = selectedPatientId,
                    patientLocations = patientLocations,
                    showSafetyZones = showSafetyZones,
                    level1Distance = safeZoneSettings.level1Distance,
                    level2Distance = safeZoneSettings.level2Distance,
                    level3Distance = safeZoneSettings.level3Distance,
                    defaultDestinationCoordinate = defaultDestination,  // 기본 목적지 좌표
                    placeLocationTrigger = placeLocationTrigger,
                    disableFollowMode = isShowingPlaceDetail || isSearchActive  // 장소 상세 정보 표시 중이거나 검색 중일 때 팔로우 모드 비활성화
                )
            }

            // 검색 중일 때 바깥 클릭 감지용 레이어 (포커스 또는 검색어 있을 때 반투명)
            if (isSearchActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = paddingValues.calculateTopPadding(),
                            bottom = 0.dp
                        )
                        .background(
                            if (isSearchFocused || searchQuery.isNotEmpty()) Color.Black.copy(alpha = 0.2f) else Color.Transparent
                        )
                        .clickable(
                            onClick = {
                                if (isSearchFocused) {
                                    // 포커스 중일 때: 키보드만 내림 (포커스 해제)
                                    focusManager.clearFocus()
                                    isSearchFocused = false
                                } else {
                                    // 포커스 해제 상태에서 다시 클릭: 검색 결과만 숨김 (검색어는 유지)
                                    viewModel.clearSearchResults()
                                }
                            },
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                        )
                )
            }

        // 검색바 + 검색 결과 (장소 상세 정보 표시 중이 아니고, 길찾기 모달이 표시되지 않을 때만)
        if (!isShowingPlaceDetail && !isNavigationModalVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(
                        top = paddingValues.calculateTopPadding() + 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (isSearchActive) 16.dp else 16.dp  // 검색 중일 때 하단 패딩 줄임
                    )
            ) {
                // 검색 입력 필드
                SearchBar(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.onFinalSearch() },
                    onFocusChanged = { isFocused ->
                        isSearchFocused = isFocused
                        if (isFocused) {
                            requestSearchFocusTrigger = false  // 포커스를 받으면 트리거 리셋
                            // 검색어가 있으면 검색 결과 다시 불러오기
                            if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                                viewModel.refreshSearch()
                            }
                        }
                    },
                    placeholder = searchPlaceholder,
                    requestFocus = requestSearchFocus || requestSearchFocusTrigger
                )

                // 길찾기 중 상태바 (길찾기 중이지만 모달이 닫혀있을 때, 검색어가 없을 때만 표시)
                if (isNavigating && !isNavigationModalVisible && searchQuery.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationStatusBar(
                        route = navigationRoute,
                        onClick = {
                            viewModel.showNavigationModal()
                        }
                    )
                }

                    // 검색 결과 리스트 (키보드 높이에 따라 동적으로 조정)
                    if (searchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // 키보드가 있을 때와 없을 때 최대 표시 개수 계산
                        val maxVisibleItems = if (imeHeightDp > 0.dp) {
                            3  // 키보드 올라왔을 때: 최대 3개
                        } else {
                            minOf(searchResults.size, 6)  // 키보드 없을 때: 최대 6개
                        }

                        val visibleItemCount = minOf(searchResults.size, maxVisibleItems)

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(77.dp * visibleItemCount)  // 동적 높이
                                .background(
                                    Color.White,
                                    RoundedCornerShape(8.dp)
                                ),
                            userScrollEnabled = searchResults.size > maxVisibleItems
                        ) {
                            items(searchResults) { place ->
                                SearchListItem(
                                    placeName = place.name,
                                    address = place.address,
                                    etaText = place.distance?.let { "${it}m" } ?: "",
                                    onClick = {
                                        focusManager.clearFocus()  // 키보드 숨김
                                        viewModel.onPlaceClick(place.id)
                                        viewModel.clearSearch()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 플로팅 버튼들 (장소 상세 정보 표시 중이 아니고, 검색 중이 아니고, 길찾기 모달이 표시되지 않을 때만)
            if (!isShowingPlaceDetail && !isSearchActive && !isNavigationModalVisible) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // 안전범위 설정 버튼 (보호자이고 안전범위가 켜져 있을 때만 표시)
                    if (userType == "GUARDIAN" && showSafetyZones) {
                        CircleFloatingButton(
                            icon = Icons.Default.Settings,
                            onClick = {
                                navController.navigate(SafeZoneSettingRoutes.SETTING)
                            },
                            containerColor = Color(0xFF8CA898),
                            contentColor = Color.White
                        )
                    }

                    // 안전범위 표시 토글 버튼
                    CircleFloatingButton(
                        icon = Icons.Default.RadioButtonChecked,
                        isToggled = showSafetyZones,
                        onClick = {
                            viewModel.safetyZoneStateManager.toggleSafetyZones()
                        }
                    )

                    // 전화 걸기 버튼
                    CircleFloatingButton(
                        icon = Icons.Default.Phone,
                        onClick = { viewModel.onClickCall(onNavigateToCall) },
                        containerColor = Color(0xFF5C7165),
                        contentColor = Color.White
                    )

                    // 도움요청 토글 버튼 (보호자만 표시)
                    if (userType == "GUARDIAN") {
                        CircleFloatingButton(
                            icon = Icons.Default.Warning,
                            isToggled = isSosEnabled,
                            onClick = {
                                selectedPatientId?.toIntOrNull()?.let { patientId ->
                                    if (isSosEnabled) {
                                        // 꺼질 때 - 종료
                                        viewModel.stopSosAlert(patientId)
                                    } else {
                                        // 켜질 때 - 시작
                                        viewModel.sendSosAlert(patientId)
                                    }
                                    isSosEnabled = !isSosEnabled
                                }
                            }
                        )
                    }

                    // 북쪽 고정 버튼
                    CircleFloatingButton(
                        icon = Icons.Default.Explore,
                        onClick = {
                            northUpTrigger++
                        }
                    )

                    // 내 위치로 이동 버튼
                    CircleFloatingButton(
                        icon = Icons.Default.MyLocation,
                        onClick = {
                            myLocationTrigger++  // 값을 증가시켜 TMapComposable에서 감지
                        }
                    )
                }
            }
        }

        // 최종 검색 결과 BottomSheet
        finalSearchResults?.let { results ->
            SearchResultBottomSheet(
                searchResults = results,
                onDismiss = { viewModel.closeFinalSearchResults() },
                onPlaceClick = { place ->
                    focusManager.clearFocus()  // 키보드 숨김
                    viewModel.onPlaceClick(place.id)
                    viewModel.closeFinalSearchResults()
                }
            )
        }

        // 장소 상세 정보 / 길찾기 Floating Panel
        // 장소 상세가 우선순위 (길찾기 중에 다른 장소를 검색한 경우)
        if (isShowingPlaceDetail || (isNavigating && isNavigationModalVisible && selectedPlaceDetail == null)) {
            MapFloatingPanel(
                placeDetail = selectedPlaceDetail,
                route = navigationRoute,
                isNavigating = isNavigating && selectedPlaceDetail == null,  // 장소 상세 표시 중이면 길찾기 모드 아님
                onDismiss = {
                    if (selectedPlaceDetail != null) {
                        // 장소 상세가 있으면: 장소 상세 닫기
                        viewModel.closePlaceDetail()
                        viewModel.clearSearch()  // 검색 상태 초기화
                        focusManager.clearFocus()  // 키보드 숨김
                        isSearchFocused = false  // 검색 포커스 상태 초기화
                    } else if (isNavigating) {
                        // 길찾기 중일 때: 모달만 닫기
                        viewModel.closeNavigationModal()
                    }
                },
                onSetDestinationClick = {
                    selectedPlaceDetail?.let { detail ->
                        android.util.Log.d("MapScreen", "목적지 설정 클릭: ${detail.name}")
                        viewModel.startNavigation(
                            endLatitude = detail.latitude,
                            endLongitude = detail.longitude,
                            endName = detail.name,
                            selectedPatientId = selectedPatientId
                        )
                        viewModel.closePlaceDetail()
                    }
                },
                onCallClick = {
                    // PlaceDetailFloatingPanel에서 시스템 전화를 걸고, 여기서는 통화 로그만 기록
                    selectedPlaceDetail?.phoneNumber?.let { phoneNumber ->
                        viewModel.logCall(phoneNumber)
                    }
                },
                onFavoriteClick = {
                    viewModel.toggleFavorite(
                        onSuccess = { message ->
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                        },
                        onError = { error ->
                            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onStopNavigationClick = {
                    android.util.Log.d("MapScreen", "길안내 중지")
                    viewModel.stopNavigation()
                }
            )
        }

        // 목적지 도착 확인 모달
        if (showArrivalDialog) {
            ArrivalConfirmationDialog(
                onConfirm = {
                    viewModel.confirmArrival()
                },
                onDismiss = {
                    viewModel.dismissArrivalDialog()
                }
            )
        }

        // 목적지 변경 확인 모달
        showDestinationChangeDialog?.let { request ->
            DestinationChangeConfirmationDialog(
                newDestinationName = request.endName,
                onConfirm = {
                    viewModel.confirmDestinationChange()
                },
                onDismiss = {
                    viewModel.cancelDestinationChange()
                }
            )
        }
    }
}

/**
 * 길찾기 중 상태바 컴포넌트
 * 검색창 아래에 표시되며, 클릭 시 길찾기 모달 다시 표시
 */
@Composable
private fun NavigationStatusBar(
    modifier: Modifier = Modifier,
    route: Route?,
    onClick: () -> Unit
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF8A9A8A),
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Explore,
                contentDescription = "길찾기 중",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                androidx.compose.material3.Text(
                    text = "길찾기 진행 중",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                route?.let {
                    androidx.compose.material3.Text(
                        text = "약 ${it.totalTimeMinutes}분 소요 · ${it.totalDistanceMeters}m",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
            androidx.compose.material3.Text(
                text = "펼치기",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 목적지 도착 확인 다이얼로그
 */
@Composable
private fun ArrivalConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "목적지 도착했습니다.\n길안내를 종료하시겠습니까?",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = Color.Black
                        )
                    ) {
                        androidx.compose.material3.Text("취소")
                    }
                    androidx.compose.material3.Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8A9A8A),
                            contentColor = Color.White
                        )
                    ) {
                        androidx.compose.material3.Text("확인")
                    }
                }
            }
        }
    }
}

/**
 * 목적지 변경 확인 다이얼로그
 */
@Composable
private fun DestinationChangeConfirmationDialog(
    newDestinationName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            shape = RoundedCornerShape(16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.Text(
                    text = "목적지를 '$newDestinationName'(으)로\n변경하시겠습니까?",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.Black
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = Color.Black
                        )
                    ) {
                        androidx.compose.material3.Text("취소")
                    }
                    androidx.compose.material3.Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8A9A8A),
                            contentColor = Color.White
                        )
                    ) {
                        androidx.compose.material3.Text("확인")
                    }
                }
            }
        }
    }
}

// Preview는 AuthStateViewModel이 필요하여 주석 처리
//@Preview(showBackground = true)
//@Composable
//fun MapScreenPreview() {
//    MapScreen(paddingValues = PaddingValues())
//}
