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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
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
    onShowBarsChange: (Boolean) -> Unit = {}  // true: 표시, false: 숨김
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
    val isNavigationMode by viewModel.isNavigationMode.collectAsState()
    val isNavigationModalVisible by viewModel.isNavigationModalVisible.collectAsState()

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

    // 실제 표시할 경로 (환자: 본인 경로, 보호자: 선택된 환자 경로)
    val displayRoute = if (userType == "GUARDIAN") {
        selectedPatientId?.toLongOrNull()?.let { id ->
            patientNavigationRoutes[id]
        }
    } else {
        navigationRoute
    }

    // 환자 위치 변경 모니터링
    LaunchedEffect(patientLocations) {
        android.util.Log.d("MapScreen", "patientLocations 변경: ${patientLocations.keys}, userType: $userType, selectedPatientId: $selectedPatientId")
    }

    // 환자 경로 변경 모니터링
    LaunchedEffect(patientNavigationRoutes, selectedPatientId) {
        android.util.Log.d("MapScreen", "patientNavigationRoutes 변경: ${patientNavigationRoutes.keys}, selectedPatientId: $selectedPatientId")
    }

    // SOS 상태 (ViewModel과 동기화)
    val isSosActiveFromViewModel by viewModel.isSosActive.collectAsState()
    var isSosEnabled by remember { mutableStateOf(isSosActiveFromViewModel) }

    // ViewModel의 SOS 상태와 동기화
    LaunchedEffect(isSosActiveFromViewModel) {
        isSosEnabled = isSosActiveFromViewModel
    }

    // 안전범위 표시 토글 상태
    var showSafetyZones by remember { mutableStateOf(false) }

    // 장소 상세 정보 표시 중 (검색창, 플로팅 버튼 숨김)
    val isShowingPlaceDetail = selectedPlaceDetail != null && !isNavigating

    // 검색창 포커스 상태
    var isSearchFocused by remember { mutableStateOf(false) }

    // 검색 중 (검색창에 포커스가 있거나, 검색 쿼리가 있거나, 검색 결과가 있을 때)
    val isSearchActive = isSearchFocused || searchQuery.isNotEmpty() || searchResults.isNotEmpty()

    // 장소 위치로 이동 트리거
    var placeLocationTrigger by remember { mutableStateOf(0 to Pair(0.0, 0.0)) }

    // 장소 상세 정보 표시 시 해당 장소로 지도 이동
    LaunchedEffect(selectedPlaceDetail) {
        val detail = selectedPlaceDetail  // 로컬 변수로 복사
        if (detail != null && !isNavigating) {
            // 장소 상세 정보 표시 시 마커 추가
            placeLocationTrigger = (placeLocationTrigger.first + 1) to Pair(
                detail.latitude,
                detail.longitude
            )
        } else if (detail == null) {
            // 장소 상세 정보가 닫혔을 때 마커 제거
            placeLocationTrigger = (placeLocationTrigger.first + 1) to Pair(0.0, 0.0)
        }
    }

    // 장소 상세 정보 또는 길찾기 모달 표시 여부에 따라 헤더/하단바 숨김/표시
    LaunchedEffect(isShowingPlaceDetail, isNavigationModalVisible) {
        onShowBarsChange(!isShowingPlaceDetail && !isNavigationModalVisible)  // 장소 상세 또는 길찾기 모달 표시 중이면 false (숨김)
    }

    // 뒤로가기 버튼 처리 (장소 상세 정보 표시 중일 때)
    val focusManager = LocalFocusManager.current
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

        // 위치 권한 요청 런처
        val locationPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        }

        // 위치 권한 요청
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
                    placeLocationTrigger = placeLocationTrigger,
                    disableFollowMode = isShowingPlaceDetail || isSearchActive  // 장소 상세 정보 표시 중이거나 검색 중일 때 팔로우 모드 비활성화
                )
            }

        // 검색바 + 검색 결과 (장소 상세 정보 표시 중이 아니고, 길찾기 모달이 표시되지 않을 때만)
        if (!isShowingPlaceDetail && !isNavigationModalVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // 검색 입력 필드
                SearchBar(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.onFinalSearch() },
                    onFocusChanged = { isFocused ->
                        isSearchFocused = isFocused
                    },
                    placeholder = searchPlaceholder,
                    requestFocus = requestSearchFocus
                )

                // 길찾기 중 상태바 (길찾기 중이지만 모달이 닫혀있을 때)
                if (isNavigating && !isNavigationModalVisible) {
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationStatusBar(
                        route = navigationRoute,
                        onClick = {
                            viewModel.showNavigationModal()
                        }
                    )
                }

                    // 검색 결과 리스트
                    if (searchResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .background(
                                    Color.White,
                                    RoundedCornerShape(8.dp)
                                )
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
                            showSafetyZones = !showSafetyZones
                        }
                    )

                    // 전화 걸기 버튼
                    CircleFloatingButton(
                        icon = Icons.Default.Phone,
                        onClick = { viewModel.onClickCall() },
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
        if (selectedPlaceDetail != null || (isNavigating && isNavigationModalVisible)) {
            MapFloatingPanel(
                placeDetail = selectedPlaceDetail,
                route = navigationRoute,
                isNavigating = isNavigating,
                onDismiss = {
                    if (isNavigating) {
                        // 길찾기 중일 때: 모달만 닫기
                        viewModel.closeNavigationModal()
                    } else {
                        // 장소 상세일 때: 장소 상세 닫기
                        viewModel.closePlaceDetail()
                        viewModel.clearSearch()  // 검색 상태 초기화
                        focusManager.clearFocus()  // 키보드 숨김
                        isSearchFocused = false  // 검색 포커스 상태 초기화
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
                    selectedPlaceDetail?.phoneNumber?.let { phoneNumber ->
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:$phoneNumber")
                        }
                        context.startActivity(intent)

                        // 통화 로그 기록
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

// Preview는 AuthStateViewModel이 필요하여 주석 처리
//@Preview(showBackground = true)
//@Composable
//fun MapScreenPreview() {
//    MapScreen(paddingValues = PaddingValues())
//}
