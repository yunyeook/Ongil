package kr.co.ongil.presentation.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kr.co.ongil.presentation.ui.common.map.CircleFloatingButton
import kr.co.ongil.service.location.LocationTrackingService

/**
 * 지도 화면
 * - TMap 표시
 * - 도움요청 토글 버튼 (플로팅 버튼)
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    // 도움요청 토글 상태
    var isSosEnabled by remember { mutableStateOf(false) }

    // 내 위치 버튼 클릭 트리거
    var myLocationTrigger by remember { mutableStateOf(0) }

    val context = LocalContext.current
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

    // 위치 추적 서비스 시작 (권한이 있을 때만)
    LaunchedEffect(hasLocationPermission) {
        if (!inPreview && hasLocationPermission) {
            val intent = Intent(context, LocationTrackingService::class.java)
                .setAction(LocationTrackingService.ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    // 화면 종료 시 위치 추적 서비스 중지
    DisposableEffect(Unit) {
        onDispose {
            if (!inPreview) {
                val stop = Intent(context, LocationTrackingService::class.java)
                    .setAction(LocationTrackingService.ACTION_STOP)
                context.startService(stop)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // TMap 표시
        TMapComposable(
            modifier = Modifier.fillMaxSize(),
            locationBus = if (inPreview) null else viewModel.locationBus,
            enableTracking = !inPreview,
            myLocationTrigger = myLocationTrigger
        )

        // 플로팅 버튼들 (화면 오른쪽 하단)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
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

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MapScreen()
}
