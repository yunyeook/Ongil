package kr.co.ongil.wear.presentation.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.*
import kr.co.ongil.wear.presentation.viewmodel.MapViewModel

/**
 * 지도 화면
 *
 * 주요 기능:
 * 1. 환자 현재 위치 표시
 * 2. 안전 범위 시각화 (3단계 원형 오버레이)
 * 3. 위치 추적 시작/중지
 * 4. 안전 범위 상태 표시
 */
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. 지도 배경
        WearTMapComposable(
            modifier = Modifier.fillMaxSize(),
            latitude = uiState.currentLatitude ?: uiState.safeZoneConfig.homeLatitude,
            longitude = uiState.currentLongitude ?: uiState.safeZoneConfig.homeLongitude,
            showCurrentLocation = uiState.hasLocation,
            showSafeZone = true,
            safeZoneConfig = uiState.safeZoneConfig
        )

        // 2. 상태 표시 오버레이 (상단)
        MapStatusOverlay(
            isTrackingActive = uiState.isTrackingActive,
            isInsideSafeZone = uiState.isInsideSafeZone,
            accuracy = uiState.accuracy,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
        )

        // 3. 제어 버튼 (하단)
        MapControls(
            isTrackingActive = uiState.isTrackingActive,
            onStartTracking = { viewModel.startLocationTracking() },
            onStopTracking = { viewModel.stopLocationTracking() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}

/**
 * 지도 상태 표시 오버레이
 */
@Composable
fun MapStatusOverlay(
    isTrackingActive: Boolean,
    isInsideSafeZone: Boolean,
    accuracy: Float?,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = {},
        modifier = modifier,
        backgroundPainter = CardDefaults.cardBackgroundPainter(
            startBackgroundColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // 추적 상태
            Text(
                text = if (isTrackingActive) "추적 중" else "추적 중지",
                style = MaterialTheme.typography.caption1,
                color = if (isTrackingActive) Color.Green else Color.Gray
            )

            // 안전 범위 상태
            if (isTrackingActive) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isInsideSafeZone) "안전 범위 내" else "안전 범위 외",
                    style = MaterialTheme.typography.caption2,
                    color = if (isInsideSafeZone) Color.Green else Color.Red
                )
            }

            // 정확도 표시
            if (accuracy != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "±${accuracy.toInt()}m",
                    style = MaterialTheme.typography.caption3,
                    color = Color.Gray
                )
            }
        }
    }
}

/**
 * 지도 제어 버튼
 */
@Composable
fun MapControls(
    isTrackingActive: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            if (isTrackingActive) {
                onStopTracking()
            } else {
                onStartTracking()
            }
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isTrackingActive) Color.Red else Color.Green
        )
    ) {
        Text(
            text = if (isTrackingActive) "중지" else "시작",
            style = MaterialTheme.typography.button,
            textAlign = TextAlign.Center
        )
    }
}
