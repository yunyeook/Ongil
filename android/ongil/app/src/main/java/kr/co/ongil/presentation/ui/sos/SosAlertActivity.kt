package kr.co.ongil.presentation.ui.sos

import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.call.IncomingCallActivity

@AndroidEntryPoint
class SosAlertActivity : ComponentActivity() {

    private val viewModel: SosAlertViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("SosAlertActivity", "🚨 SOS Alert Activity 시작")

        // 잠금 화면 위에 띄우고 화면 켜기
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        dismissKeyguardIfNeeded()

        // FCM에서 넘어온 데이터 읽기
        val sosId = intent.getLongExtra("sosId", 0L)
        val senderName = intent.getStringExtra("senderName") ?: "보호자"

        Log.d("SosAlertActivity", "sosId: $sosId, senderName: $senderName")

        // 기본 보호자 정보 가져오기
        viewModel.loadDefaultGuardian()

        setContent {
            OngilTheme {
                val uiState by viewModel.uiState.collectAsState()

                SosAlertScreen(
                    senderName = senderName,
                    defaultGuardian = uiState.defaultGuardian,
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onCallClick = {
                        uiState.defaultGuardian?.let { guardian ->
                            Log.d("SosAlertActivity", "✅ 전화 걸기 - guardianId: ${guardian.id}")
                            // VoIP 통화 시작
                            startVoipCall(guardian.id, guardian.name, guardian.phoneNumber)
                        }
                    },
                    onDismiss = {
                        Log.d("SosAlertActivity", "닫기 클릭")
                        finish()
                    }
                )
            }
        }
    }

    private fun dismissKeyguardIfNeeded() {
        val km = getSystemService(KeyguardManager::class.java)
        if (km.isKeyguardLocked) {
            km.requestDismissKeyguard(this, null)
        }
    }

    private fun startVoipCall(guardianId: Long, guardianName: String, guardianPhone: String) {
        // IncomingCallActivity를 사용하지 않고, MainActivity의 VoipCall 화면으로 이동
        val intent = Intent(this, kr.co.ongil.presentation.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigateTo", "voip_call")
            putExtra("receiverId", guardianId)
            putExtra("receiverName", guardianName)
            putExtra("receiverPhone", guardianPhone)
            putExtra("isCaller", true)
            putExtra("userType", "GUARDIAN") // 전화 받는 사람은 보호자
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun SosAlertScreen(
    senderName: String,
    defaultGuardian: kr.co.ongil.presentation.ui.favorite.PatientData?,
    isLoading: Boolean,
    error: String?,
    onCallClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 제목
            Text(
                text = "🚨 도움 요청",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B6B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 메시지
            Text(
                text = "${senderName}님이 도움을 요청했습니다",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "보호자에게 전화를 걸어주세요",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 로딩 또는 에러 표시
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text(
                        text = error,
                        fontSize = 14.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                defaultGuardian != null -> {
                    // 기본 보호자 정보 표시
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = defaultGuardian.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = defaultGuardian.phoneNumber,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 전화걸기 버튼
            Button(
                onClick = onCallClick,
                enabled = defaultGuardian != null && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    text = "📞 전화 걸기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 닫기 버튼
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "닫기",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
