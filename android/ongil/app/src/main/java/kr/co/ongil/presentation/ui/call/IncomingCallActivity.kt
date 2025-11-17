package kr.co.ongil.presentation.ui.call

import android.app.KeyguardManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import dagger.hilt.android.AndroidEntryPoint
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.call.VoipCallScreen
import kr.co.ongil.presentation.ui.call.VoipIncomingCallScreen

@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 잠금 화면 위에 띄우고 화면 켜기
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        dismissKeyguardIfNeeded()

        // FCM에서 넘어온 데이터 읽기
        val callId = intent.getLongExtra("callId", 0L)
        val sessionId = intent.getStringExtra("sessionId")
        val callerName = intent.getStringExtra("callerName") ?: "알 수 없음"
        val callerPhone = intent.getStringExtra("callerPhone") ?: ""
        val userType = intent.getStringExtra("userType") ?: "PATIENT"

        setContent {
            OngilTheme {
                // 수락 전/후를 같은 Activity 안에서 상태로 관리
                var accepted by remember { mutableStateOf(false) }

                if (!accepted) {
                    // 수신 화면
                    VoipIncomingCallScreen(
                        callerName = callerName,
                        callerPhone = callerPhone,
                        callId = callId,
                        userType = userType,
                        sessionId = sessionId,
                        onAccepted = {
                            // 여기서는 NavController 안 쓰고,
                            // 그냥 같은 Activity 안에서 "통화 중 화면"으로 전환
                            accepted = true
                        },
                        onDeclined = {
                            finish()
                        }
                    )
                } else {
                    // 통화 중 화면 (기존 VoipCallScreen 재사용)
                    VoipCallScreen(
                        targetName = callerName,
                        targetPhone = callerPhone,
                        isCaller = false,          // 나는 수신자
                        userType = userType,
                        callId = callId,
                        receiverId = null,
                        onCallEnded = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun dismissKeyguardIfNeeded() {
        val km = getSystemService(KeyguardManager::class.java)
        if (km.isKeyguardLocked) {
            km.requestDismissKeyguard(this, null)
        }
    }
}
