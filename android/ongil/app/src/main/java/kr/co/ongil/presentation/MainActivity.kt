package kr.co.ongil.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dagger.hilt.android.AndroidEntryPoint
import kr.co.ongil.presentation.navigation.MainScreen
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.Atest.PlayGroundGK
//import kr.co.ongil.presentation.ui.Atest.PlayGroundMJ
import kr.co.ongil.presentation.ui.Atest.PlayGroundSH
import kr.co.ongil.presentation.ui.call.VoipCallDebugScreen


// 나중에 커밋할때는 플레이그라운드 다 주석처리해주세요
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // FCM 수신 통화 데이터
    private var incomingCallData by mutableStateOf<IncomingCallData?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // FCM으로부터 전달된 Intent 처리
        handleIncomingCallIntent(intent)

        setContent {
            OngilTheme {
//                VoipCallDebugScreen()
                 MainScreen(
                     incomingCallData = incomingCallData,
                     onIncomingCallHandled = { incomingCallData = null }
                 )
//                PlayGroundMJ()
                // PlayGroundSH()
                // PlayGroundGK()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingCallIntent(intent)
    }

    private fun handleIncomingCallIntent(intent: Intent?) {
        if (intent?.getStringExtra("type") == "INCOMING_CALL") {
            val callId = intent.getLongExtra("callId", 0L)
            val sessionId = intent.getStringExtra("sessionId")
            val callerName = intent.getStringExtra("callerName") ?: ""
            val callerPhone = intent.getStringExtra("callerPhone") ?: ""
            val userType = "PATIENT"  // TODO: 실제 사용자 role로 변경

            if (callId > 0) {
                incomingCallData = IncomingCallData(
                    callId = callId,
                    sessionId = sessionId,
                    callerName = callerName,
                    callerPhone = callerPhone,
                    userType = userType
                )
            }
        }
    }
}

/**
 * FCM 수신 통화 데이터
 */
data class IncomingCallData(
    val callId: Long,
    val sessionId: String?,
    val callerName: String,
    val callerPhone: String,
    val userType: String
)