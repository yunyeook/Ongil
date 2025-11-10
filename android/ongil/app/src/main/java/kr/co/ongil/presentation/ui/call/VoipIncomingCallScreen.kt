package kr.co.ongil.presentation.ui.call
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * VOIP 수신 화면
 *
 * - 전화 왔을 때 보여주는 전체 화면
 * - 발신자 이름/번호/프로필 보여주고, 수락/거절 버튼 제공
 * - 수락: VoipCallViewModel.acceptCall() 호출 후, 상위에서 통화중 화면으로 네비게이션
 * - 거절: VoipCallViewModel.endCall() 또는 추후 REJECTED 상태용 핸들러 연결
 */
@Composable
fun VoipIncomingCallScreen(
    callerName: String,
    callerPhone: String,
    callId: Long,
    userType: String, // "PATIENT" or "GUARDIAN"
    sessionId: String? = null,  // FCM에서 전달된 sessionId
    onAccepted: () -> Unit,     // 수락 후 통화중 화면으로 전환용 콜백
    onDeclined: () -> Unit,     // 거절/종료 후 나가기 콜백
    viewModel: VoipCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // FCM 수신 통화 초기화 (WebSocket 연결 + 통화 정보 로드)
    LaunchedEffect(callId, sessionId) {
        viewModel.initIncomingCall(callId, sessionId)
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D141E)),
        color = Color(0xFF0D141E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.height(40.dp))

            // 상단 상태 텍스트
            Text(
                text = "수신 전화",
                color = Color(0xFF9FA9B8),
                fontSize = 16.sp
            )

            Spacer(Modifier.height(24.dp))

            // 프로필 동그라미
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222A36)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callerName.take(1),
                    color = Color(0xFF5C6675),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            // 발신자 이름
            Text(
                text = callerName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // 발신자 번호
            Text(
                text = callerPhone,
                color = Color(0xFF9FA9B8),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            // 안내 문구/에러
            uiState.error?.let {
                Text(
                    text = it,
                    color = Color(0xFFFF6B81),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
            } ?: run {
                Text(
                    text = "통화를 수락하시겠습니까?",
                    color = Color(0xFF9FA9B8),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(40.dp))

            // 하단 수락 / 거절 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 거절 버튼 (빨간 수화기)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4757))
                        .clickable {
                            // 현재는 ENDED로 처리 (원하면 REJECTED용 API 추가)
                            viewModel.endCall()
                            onDeclined()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "거절",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // 수락 버튼 (초록 수화기)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2ED573))
                        .clickable {
                            // 통화 수락 처리
                            viewModel.acceptCall(userType = userType)
                            onAccepted()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "수락",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Preview(
    name = "수신 전화",
    showBackground = true
)
@Composable
private fun VoipIncomingCallScreenPreview() {
    VoipIncomingCallScreenPreviewContent(
        callerName = "김할머니",
        callerPhone = "010-1234-5678"
    )
}

@Preview(
    name = "수신 전화 - 에러",
    showBackground = true
)
@Composable
private fun VoipIncomingCallScreenErrorPreview() {
    VoipIncomingCallScreenPreviewContent(
        callerName = "박보호자",
        callerPhone = "010-9876-5432",
        errorMessage = "⚠️ 위치 권한이 필요합니다"
    )
}

@Composable
private fun VoipIncomingCallScreenPreviewContent(
    callerName: String,
    callerPhone: String,
    errorMessage: String? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D141E)),
        color = Color(0xFF0D141E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Spacer(Modifier.height(40.dp))

            // 상단 상태 텍스트
            Text(
                text = "수신 전화",
                color = Color(0xFF9FA9B8),
                fontSize = 16.sp
            )

            Spacer(Modifier.height(24.dp))

            // 프로필 동그라미
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222A36)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = callerName.take(1),
                    color = Color(0xFF5C6675),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            // 발신자 이름
            Text(
                text = callerName,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            // 발신자 번호
            Text(
                text = callerPhone,
                color = Color(0xFF9FA9B8),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            // 안내 문구/에러
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color(0xFFFF6B81),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
            } ?: run {
                Text(
                    text = "통화를 수락하시겠습니까?",
                    color = Color(0xFF9FA9B8),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(40.dp))

            // 하단 수락 / 거절 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 거절 버튼 (빨간 수화기)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF4757))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "거절",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // 수락 버튼 (초록 수화기)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2ED573))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "수락",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}
