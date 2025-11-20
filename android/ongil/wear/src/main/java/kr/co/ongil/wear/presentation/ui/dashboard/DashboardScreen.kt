package kr.co.ongil.wear.presentation.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.material.*
import kr.co.ongil.wear.presentation.viewmodel.DashboardViewModel

/**
 * 대시보드 화면 (메인 화면)
 *
 * 주요 기능:
 * 1. 전화 버튼 - 핫라인 통화
 * 2. 지도 버튼 - 현재 위치 + 안전 범위
 * 3. 도움 버튼 - SOS 요청
 * 4. 상태 표시 - 위치 추적, 안전 범위
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToMap: () -> Unit,
    onNavigateToCall: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        timeText = {
            TimeText()
        },
        vignette = {
            Vignette(vignettePosition = VignettePosition.TopAndBottom)
        }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 32.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 상태 표시
            item {
                StatusCard(
                    locationTrackingActive = uiState.locationTrackingActive,
                    insideSafeZone = uiState.insideSafeZone
                )
            }

            // 전화 버튼
            item {
                DashboardButton(
                    icon = Icons.Default.Call,
                    label = "전화",
                    description = "보호자에게 전화",
                    backgroundColor = Color(0xFF4CAF50),
                    onClick = onNavigateToCall
                )
            }

            // 지도 버튼
            item {
                DashboardButton(
                    icon = Icons.Default.LocationOn,
                    label = "지도",
                    description = "현재 위치 확인",
                    backgroundColor = Color(0xFF2196F3),
                    onClick = onNavigateToMap
                )
            }

            // 도움 버튼
            item {
                DashboardButton(
                    icon = Icons.Default.Warning,
                    label = "도움",
                    description = "긴급 도움 요청",
                    backgroundColor = Color(0xFFF44336),
                    onClick = onNavigateToHelp
                )
            }
        }
    }
}

/**
 * 상태 표시 카드
 */
@Composable
private fun StatusCard(
    locationTrackingActive: Boolean,
    insideSafeZone: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 위치 추적 상태
            Text(
                text = if (locationTrackingActive) "위치 추적 중" else "위치 추적 꺼짐",
                style = MaterialTheme.typography.caption1,
                color = if (locationTrackingActive) Color.Green else Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 안전 범위 상태
            if (locationTrackingActive) {
                Text(
                    text = if (insideSafeZone) "안전 범위 내" else "안전 범위 외",
                    style = MaterialTheme.typography.caption2,
                    color = if (insideSafeZone) Color.Green else Color.Red
                )
            }
        }
    }
}

/**
 * 대시보드 버튼
 */
@Composable
private fun DashboardButton(
    icon: ImageVector,
    label: String,
    description: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Chip(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.title3,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        secondaryLabel = {
            Text(
                text = description,
                style = MaterialTheme.typography.caption2,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
        },
        colors = ChipDefaults.primaryChipColors(
            backgroundColor = backgroundColor
        )
    )
}
