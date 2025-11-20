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
 * Wear OS 수신 통화 화면 (블루투스 모델)
 *
 * 주요 기능:
 * - 수신 통화 알림 표시
 * - 수락/거절 버튼
 *
 * 참고:
 * - Phone으로부터 FCM을 통해 callId와 발신자 정보를 받음
 * - Watch는 UI만 표시, WebRTC는 Phone에서 처리
 */
@Composable
fun IncomingCallScreen(
    callId: Long,
    callerUserId: String,
    callerName: String,
    onCallAccepted: () -> Unit = {},
    onCallRejected: () -> Unit = {},
    viewModel: WearCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 최초 진입 시 수신 통화 정보 로드
    LaunchedEffect(Unit) {
        viewModel.loadIncomingCall(
            callId = callId,
            callerUserId = callerUserId,
            callerName = callerName
        )
    }

    // 수락/거절 후 화면 전환
    LaunchedEffect(uiState.callState?.status) {
        when (uiState.callState?.status) {
            CallStatus.CONNECTED -> onCallAccepted()
            CallStatus.ENDED, CallStatus.FAILED -> onCallRejected()
            else -> {}
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
                // 발신자 이름
                Text(
                    text = callerName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 수신 중 메시지
                Text(
                    text = "수신 중...",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 수락/거절 버튼
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 거절 버튼
                    Button(
                        onClick = {
                            viewModel.rejectCall()
                        },
                        modifier = Modifier.size(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Red
                        ),
                        shape = CircleShape,
                        enabled = !uiState.isLoading
                    ) {
                        Text(
                            text = "거절",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }

                    // 수락 버튼
                    Button(
                        onClick = {
                            viewModel.acceptCall()
                        },
                        modifier = Modifier.size(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Green
                        ),
                        shape = CircleShape,
                        enabled = !uiState.isLoading
                    ) {
                        Text(
                            text = "수락",
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
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
