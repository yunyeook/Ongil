package kr.co.ongil.presentation.ui.map

import androidx.compose.foundation.background
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kr.co.ongil.data.model.location.Coordinate
import kr.co.ongil.presentation.ui.auth.AuthStateViewModel
import kr.co.ongil.presentation.ui.common.map.CircleFloatingButton
import kr.co.ongil.presentation.ui.common.map.SearchBar
import kr.co.ongil.presentation.ui.common.map.SearchListItem
import kr.co.ongil.service.location.LocationTrackingService

/**
 * 지도 화면
 * - TMap 표시
 * - 장소 검색 (실시간)
 * - 도움요청 토글 버튼 (플로팅 버튼)
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    viewModel: MapViewModel = hiltViewModel(),
    authViewModel: AuthStateViewModel
) {
    // ViewModel 상태
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val finalSearchResults by viewModel.finalSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // 사용자 정보
    val currentUserInfo by authViewModel.currentUserInfo.collectAsState(initial = null)
    val userType = currentUserInfo?.getOrNull()?.userType ?: ""
    val selectedPatientId by authViewModel.selectedPatientId.collectAsState()

    // 환자 위치 정보 (보호자용)
    val patientLocations by authViewModel.patientLocations.collectAsState()

    // 환자 위치 변경 모니터링
    LaunchedEffect(patientLocations) {
        android.util.Log.d("MapScreen", "patientLocations 변경: ${patientLocations.keys}, userType: $userType, selectedPatientId: $selectedPatientId")
    }

    // 도움요청 토글 상태
    var isSosEnabled by remember { mutableStateOf(false) }

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
                    userType = userType,
                    selectedPatientId = selectedPatientId,
                    patientLocations = patientLocations
                )
            }

        // 검색바 + 검색 결과 (화면 상단)
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
                onSearch = { viewModel.onFinalSearch() }
            )

                // 검색 결과 리스트
                if (searchResults.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .background(
                                Color.White,
                                androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                    ) {
                        items(searchResults) { place ->
                            SearchListItem(
                                placeName = place.name,
                                address = place.address,
                                etaText = "", // TODO: 거리 계산
                                onClick = {
                                    // TODO: 지도에 마커 표시 및 이동
                                    viewModel.clearSearch()
                                }
                            )
                        }
                    }
                }
            }

            // 플로팅 버튼들 (화면 오른쪽 하단)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {

                // 전화 걸기 버튼
                CircleFloatingButton(
                    icon = Icons.Default.Phone,
                    onClick = { viewModel.onClickCall() },
                    containerColor = Color(0xFF5C7165),
                    contentColor = Color.White
                )

                // 도움요청 토글 버튼
                CircleFloatingButton(
                    icon = Icons.Default.Warning,
                    isToggled = isSosEnabled,
                    onClick = {
                        isSosEnabled = !isSosEnabled
                        // TODO: 도움요청 토글 상태 변경 시 로직
                        if (isSosEnabled) {
                            // 도움요청 활성화
                        } else {
                            // 도움요청 비활성화
                        }
                    }
                )

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

        // 최종 검색 결과 BottomSheet
        finalSearchResults?.let { results ->
            SearchResultBottomSheet(
                searchResults = results,
                onDismiss = { viewModel.closeFinalSearchResults() },
                onPlaceClick = { place ->
                    // TODO: 지도에 마커 표시 및 이동
                    viewModel.closeFinalSearchResults()
                }
            )
        }
    }
}

// Preview는 AuthStateViewModel이 필요하여 주석 처리
//@Preview(showBackground = true)
//@Composable
//fun MapScreenPreview() {
//    MapScreen(paddingValues = PaddingValues())
//}
