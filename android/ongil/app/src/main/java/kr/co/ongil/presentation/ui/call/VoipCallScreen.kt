
package kr.co.ongil.presentation.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun VoipCallScreen(
    // nav에서 넘겨줄 값들 (예시)
    targetName: String,
    targetPhone: String,
    isCaller: Boolean,        // true: 내가 거는 중, false: 내가 받는 쪽
    userType: String,         // "PATIENT" or "GUARDIAN" 등
    callId: Long? = null,     // 수신 화면에서 진입 시 서버에서 받은 callId
    receiverId: Long? = null, // 발신 시 상대 userId (startVoipCall용)
    onCallEnded: () -> Unit = {},
    viewModel: VoipCallViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 🔁 최초 진입 시 동작
    LaunchedEffect(Unit) {
        if (isCaller && receiverId != null) {
            // 발신자: 통화 세션 생성 + 위치 전송 + WebRTC 준비
            viewModel.startVoipCall(receiverId = receiverId, userType = userType)
        } else if (!isCaller && callId != null) {
            // 수신자: WebSocket 연결 + 구독 + 통화 정보 조회
            viewModel.initIncomingCall(callId, null)
        }
    }

    // 🕒 간단 타이머 (CONNECTED 이후부터 증가)
    var seconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(uiState.call?.status) {
        android.util.Log.d("VoipCallScreen", "Timer LaunchedEffect: status=${uiState.call?.status}")
        if (uiState.call?.status == "CONNECTED") {
            android.util.Log.d("VoipCallScreen", "✅ Timer started")
            while (true) {
                delay(1000)
                seconds++
                android.util.Log.d("VoipCallScreen", "Timer tick: $seconds seconds")
            }
        } else {
            android.util.Log.d("VoipCallScreen", "Timer reset (status not CONNECTED)")
            seconds = 0
        }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            // 상단 "통화 중" + 초록 점
            Text(
                text = if (uiState.call?.status == "CONNECTED") "통화 중" else "연결 중",
                color = Color(0xFF9FA9B8),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2ED573))
            )

            Spacer(Modifier.height(40.dp))

            // 프로필 동그라미 (이미지 없으니 placeholder)
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222A36)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = targetName.take(1),
                    color = Color(0xFF5C6675),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            // 이름
            Text(
                text = targetName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // 번호
            Text(
                text = targetPhone,
                color = Color(0xFF9FA9B8),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(24.dp))

            // 통화 시간
            Text(
                text = formatSeconds(seconds),
                color = Color(0xFF2ED573),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(48.dp))

            // 하단 기능 버튼들 (스피커, 도움요청)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallFeatureButton(
                    icon = Icons.Default.VolumeUp,
                    label = "스피커폰",
                    onClick = { /* TODO: 스피커 토글 */ }
                )
                CallFeatureButton(
                    icon = Icons.Default.Sos,
                    label = "도움 요청",
                    onClick = {
                        // TODO: SOS / 보호자 호출 등 연결
                    }
                )
            }

            Spacer(Modifier.height(48.dp))

            // 종료 버튼 (크게)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF4757))
                    .clickable {
                        viewModel.endCall()
                        onCallEnded()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "통화 종료",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // 녹음 중/메시지/에러 표시
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = "● 녹음 중",
                    color = Color(0xFF9FA9B8),
                    fontSize = 12.sp
                )

                uiState.message?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        color = Color(0xFF2ED573),
                        fontSize = 12.sp
                    )
                }

                uiState.error?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = it,
                        color = Color(0xFFFF6B81),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CallFeatureButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .background(Color(0xFF1C2430), CircleShape)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = Color(0xFF9FA9B8),
            fontSize = 11.sp
        )
    }
}

private fun formatSeconds(total: Int): String {
    val m = total / 60
    val s = total % 60
    return String.format(java.util.Locale.getDefault(), "%02d:%02d", m, s)
}

@Preview(
    name = "통화 중",
    showBackground = true
)
@Composable
private fun VoipCallScreenConnectedPreview() {
    VoipCallScreenPreviewContent(
        targetName = "김할머니",
        targetPhone = "010-1234-5678",
        callStatus = "CONNECTED",
        seconds = 125
    )
}

@Preview(
    name = "연결 중",
    showBackground = true
)
@Composable
private fun VoipCallScreenConnectingPreview() {
    VoipCallScreenPreviewContent(
        targetName = "박보호자",
        targetPhone = "010-9876-5432",
        callStatus = "CREATED",
        seconds = 0
    )
}

@Composable
private fun VoipCallScreenPreviewContent(
    targetName: String,
    targetPhone: String,
    callStatus: String,
    seconds: Int
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(40.dp))

            // 상단 "통화 중" + 초록 점
            Text(
                text = if (callStatus == "CONNECTED") "통화 중" else "연결 중",
                color = Color(0xFF9FA9B8),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2ED573))
            )

            Spacer(Modifier.height(40.dp))

            // 프로필 동그라미
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222A36)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = targetName.take(1),
                    color = Color(0xFF5C6675),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            // 이름
            Text(
                text = targetName,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            // 번호
            Text(
                text = targetPhone,
                color = Color(0xFF9FA9B8),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(24.dp))

            // 통화 시간
            Text(
                text = formatSeconds(seconds),
                color = Color(0xFF2ED573),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(48.dp))

            // 하단 기능 버튼들
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CallFeatureButton(
                    icon = Icons.Default.VolumeUp,
                    label = "스피커폰",
                    onClick = {}
                )
                CallFeatureButton(
                    icon = Icons.Default.Sos,
                    label = "도움 요청",
                    onClick = {}
                )
            }

            Spacer(Modifier.height(48.dp))

            // 종료 버튼
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
                    contentDescription = "통화 종료",
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            // 녹음 중/메시지/에러 표시
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "● 녹음 중",
                    color = Color(0xFF9FA9B8),
                    fontSize = 12.sp
                )
            }
        }
    }
}