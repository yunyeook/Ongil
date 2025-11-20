package kr.co.ongil.wear.presentation.ui.call

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
import kr.co.ongil.wear.domain.model.CallStatus
import kr.co.ongil.wear.presentation.viewmodel.WearCallViewModel

/**
 * Wear OS VoIP 통화 화면 (블루투스 모델)
 *
 * 주요 기능:
 * - 통화 중 화면 표시
 * - 통화 시간 표시
 * - 종료 버튼
 *
 * 참고:
 * - WebRTC 오디오는 Phone에서 처리
 * - Watch는 UI만 표시
 */
@Composable
fun CallScreen(
    targetName: String,
    targetUserId: String,
    targetPhone: String,
    isCaller: Boolean,
    onCallEnded: () -> Unit = {},
    viewModel: WearCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 최초 진입 시 통화 시작 (발신자인 경우)
    LaunchedEffect(Unit) {
        if (isCaller) {
            viewModel.startVoipCall(
                targetUserId = targetUserId,
                targetName = targetName,
                targetPhone = targetPhone
            )
        }
    }

    // 통화 종료 시 화면 닫기
    LaunchedEffect(uiState.callState?.status) {
        if (uiState.callState?.status == CallStatus.ENDED ||
            uiState.callState?.status == CallStatus.FAILED
        ) {
            onCallEnded()
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 상대방 이름
                Text(
                    text = targetName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 통화 상태 메시지
                Text(
                    text = when (uiState.callState?.status) {
                        CallStatus.CALLING -> "연결 중..."
                        CallStatus.RINGING -> "벨 울리는 중..."
                        CallStatus.CONNECTING -> "연결 중..."
                        CallStatus.CONNECTED -> formatDuration(uiState.callState?.duration ?: 0)
                        else -> uiState.message ?: "통화 준비 중..."
                    },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 종료 버튼
                Button(
                    onClick = {
                        viewModel.endCall()
                    },
                    modifier = Modifier
                        .size(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Red
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = "종료",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                // 에러 메시지 표시
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        fontSize = 12.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // 로딩 표시
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

/**
 * 통화 시간 포맷팅 (mm:ss)
 */
private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}
