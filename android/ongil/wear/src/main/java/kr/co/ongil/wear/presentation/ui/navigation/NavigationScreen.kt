package kr.co.ongil.wear.presentation.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.*
import kr.co.ongil.wear.presentation.ui.navigation.component.ArrowNavigationIndicator
import kr.co.ongil.wear.presentation.viewmodel.NavigationViewModel

/**
 * Wear OS 네비게이션 화면 (블루투스 모델)
 *
 * 주요 기능:
 * - 화살표 기반 방향 안내
 * - 남은 거리 표시
 * - 경로 이탈 알림
 * - 목적지 도착 알림
 *
 * 참고:
 * - 경로 데이터는 Phone으로부터 sync (TODO)
 * - Watch는 UI만 표시
 */
@Composable
fun NavigationScreen(
    navigationId: Long,
    onNavigationEnded: () -> Unit = {},
    viewModel: NavigationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 네비게이션 종료 시 화면 닫기
    LaunchedEffect(uiState.isNavigating) {
        if (!uiState.isNavigating && uiState.isArrived) {
            onNavigationEnded()
        }
    }

    Scaffold(
        timeText = {
            TimeText()
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1C1E)),
            contentAlignment = Alignment.Center
        ) {
            when {
                // 도착 완료
                uiState.isArrived -> {
                    ArrivedContent(
                        destinationName = uiState.destinationName ?: "목적지",
                        onDismiss = onNavigationEnded
                    )
                }

                // 응급상황 (5분 이상 경로 이탈)
                uiState.isEmergency -> {
                    EmergencyContent(
                        onCancel = {
                            viewModel.cancelNavigation()
                        }
                    )
                }

                // 경로 이탈
                uiState.isRouteDeviated -> {
                    DeviationContent(
                        deviationDistanceMeters = uiState.deviationDistanceMeters,
                        onCancel = {
                            viewModel.cancelNavigation()
                        }
                    )
                }

                // 정상 네비게이션
                uiState.isNavigating -> {
                    NavigationContent(
                        destinationName = uiState.destinationName ?: "목적지",
                        relativeBearing = uiState.relativeBearing,
                        distanceToNextMeters = uiState.distanceToNextWaypointMeters,
                        onCancel = {
                            viewModel.cancelNavigation()
                        }
                    )
                }

                // 네비게이션 대기
                else -> {
                    LoadingContent()
                }
            }
        }
    }
}

/**
 * 정상 네비게이션 콘텐츠
 */
@Composable
private fun NavigationContent(
    destinationName: String,
    relativeBearing: Float,
    distanceToNextMeters: Int,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 목적지 이름
        Text(
            text = destinationName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 화살표 방향 표시
        ArrowNavigationIndicator(
            relativeBearing = relativeBearing,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 남은 거리
        Text(
            text = formatDistance(distanceToNextMeters),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Green,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 취소 버튼
        Button(
            onClick = onCancel,
            modifier = Modifier.size(48.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red
            ),
            shape = CircleShape
        ) {
            Text(
                text = "X",
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}

/**
 * 경로 이탈 콘텐츠
 */
@Composable
private fun DeviationContent(
    deviationDistanceMeters: Int,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "경로 이탈",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "경로에서 ${deviationDistanceMeters}m 벗어났습니다",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red
            )
        ) {
            Text(
                text = "취소",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

/**
 * 응급상황 콘텐츠
 */
@Composable
private fun EmergencyContent(
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "응급상황!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "5분 이상 경로 이탈",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "보호자에게 알림이 전송됩니다",
            fontSize = 11.sp,
            color = Color.Yellow,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red
            )
        ) {
            Text(
                text = "취소",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

/**
 * 도착 완료 콘텐츠
 */
@Composable
private fun ArrivedContent(
    destinationName: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "도착!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Green,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = destinationName,
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Green
            )
        ) {
            Text(
                text = "확인",
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

/**
 * 로딩 콘텐츠
 */
@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            strokeWidth = 3.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "경로 로드 중...",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 거리 포맷팅
 */
private fun formatDistance(meters: Int): String {
    return when {
        meters >= 1000 -> String.format("%.1f km", meters / 1000.0)
        else -> "${meters}m"
    }
}
