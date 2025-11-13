package kr.co.ongil.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kr.co.ongil.presentation.navigation.MainScreen
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.auth.AuthStateViewModel


// 나중에 커밋할때는 플레이그라운드 다 주석처리해주세요
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthStateViewModel by viewModels()

    // FCM 수신 통화 데이터
    private var incomingCallData by mutableStateOf<IncomingCallData?>(null)

    // 알림 권한 요청 런처 (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            android.util.Log.d("MainActivity", "알림 권한 승인됨")
        } else {
            android.util.Log.w("MainActivity", "알림 권한 거부됨")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android 13+ 알림 권한 요청
        requestNotificationPermission()

        // FCM으로부터 전달된 Intent 처리
        handleIncomingCallIntent(intent)

        setContent {
            OngilTheme {
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

            if (callId > 0) {
                lifecycleScope.launch {
                    // AuthStateViewModel에서 실제 사용자 타입 가져오기
                    val userType = authViewModel.currentUserInfo.firstOrNull()
                        ?.getOrNull()
                        ?.userType
                        ?: "PATIENT"  // 기본값

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

    // Android 13+ 알림 권한 요청
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // 이미 권한이 있음
                    android.util.Log.d("MainActivity", "알림 권한 이미 승인됨")
                }
                else -> {
                    // 권한 요청
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
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