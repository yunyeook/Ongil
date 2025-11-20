package kr.co.ongil.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
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
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.ongil.presentation.navigation.MainScreen
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.auth.AuthStateViewModel
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject


// 나중에 커밋할때는 플레이그라운드 다 주석처리해주세요
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthStateViewModel by viewModels()

    // ✅ UserDataStoreManager 주입
    @Inject
    lateinit var userDataStoreManager: kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager

    // FCM 수신 통화 데이터
    private var incomingCallData by mutableStateOf<IncomingCallData?>(null)

    // 응급 전화 데이터
    private var emergencyCallData by mutableStateOf<EmergencyCallData?>(null)

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

        // ✅ AccessToken 변화 감지 - 로그인하면 자동으로 FCM 동기화
        lifecycleScope.launch {
            userDataStoreManager.getAccessToken().collect { accessToken ->
                if (!accessToken.isNullOrBlank()) {
                    android.util.Log.d("MainActivity", "🔑 AccessToken 감지 - FCM 동기화 시작")
                    syncFcmToken()
                }
            }
        }

        requestNotificationPermission()
        handleIncomingCallIntent(intent)
        handleEmergencyCallIntent(intent)

        if (intent.getStringExtra("type") == "INCOMING_CALL") {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        setContent {
            OngilTheme {
                MainScreen(
                    incomingCallData = incomingCallData,
                    onIncomingCallHandled = { incomingCallData = null },
                    emergencyCallData = emergencyCallData,
                    onEmergencyCallHandled = { emergencyCallData = null },
                    notificationNavigation = notificationNavigation,
                    onNotificationNavigationHandled = { notificationNavigation = null}
                )
            }
        }
    }

    private suspend fun syncFcmToken() {
        try {
            android.util.Log.d("MainActivity", "========== FCM 동기화 시작 ==========")

            // AccessToken 확인
            val accessToken = userDataStoreManager.getAccessToken().firstOrNull()
            if (accessToken.isNullOrBlank()) {
                android.util.Log.d("MainActivity", "❌ AccessToken 없음, 동기화 스킵")
                return
            }
            android.util.Log.d("MainActivity", "✅ AccessToken 확인됨")

            // 동기화 여부 확인
            val isSynced = userDataStoreManager.isFcmTokenSynced().firstOrNull() ?: false
            android.util.Log.d("MainActivity", "동기화 상태: $isSynced")

            if (isSynced) {
                android.util.Log.d("MainActivity", "✅ 이미 동기화됨, 스킵")
                return
            }

            // FCM 토큰 가져오기
            var currentToken = userDataStoreManager.getFcmToken().firstOrNull()
            android.util.Log.d("MainActivity", "로컬 토큰: ${currentToken?.take(20)}...")

            if (currentToken.isNullOrBlank()) {
                android.util.Log.d("MainActivity", "로컬 토큰 없음, Firebase에서 조회")
                currentToken = try {
                    com.google.firebase.messaging.FirebaseMessaging
                        .getInstance()
                        .token
                        .await()
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Firebase 토큰 조회 실패", e)
                    null
                }

                if (!currentToken.isNullOrBlank()) {
                    userDataStoreManager.saveFcmToken(currentToken)
                    android.util.Log.d("MainActivity", "✅ 토큰 로컬 저장 완료")
                }
            }

            if (currentToken.isNullOrBlank()) {
                android.util.Log.e("MainActivity", "❌ FCM 토큰을 가져올 수 없음")
                return
            }

            android.util.Log.d("MainActivity", "🚀 서버로 토큰 전송 시작: ${currentToken.take(20)}...")

            // 서버로 전송
            val success = sendTokenToServer(currentToken, accessToken)

            if (success) {
                userDataStoreManager.setFcmTokenSynced(true)
                android.util.Log.d("MainActivity", "✅✅✅ FCM 토큰 서버 동기화 완료 ✅✅✅")
            } else {
                android.util.Log.e("MainActivity", "❌ FCM 토큰 서버 전송 실패")
            }

            android.util.Log.d("MainActivity", "========== FCM 동기화 종료 ==========")

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "FCM 토큰 동기화 실패", e)
        }
    }

    private suspend fun sendTokenToServer(fcmToken: String, accessToken: String): Boolean {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val json = org.json.JSONObject().apply { put("token", fcmToken) }
                val body = json.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = okhttp3.Request.Builder()
                    .url("${kr.co.ongil.BuildConfig.BASE_URL}api/v1/fcm/register")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(body)
                    .build()

                android.util.Log.d("MainActivity", "📡 HTTP 요청 전송 중...")
                android.util.Log.d("MainActivity", "URL: ${kr.co.ongil.BuildConfig.BASE_URL}api/v1/fcm/register")

                client.newCall(request).execute().use { response ->
                    val responseCode = response.code
                    val responseBody = response.body?.string()

                    android.util.Log.d("MainActivity", "📡 HTTP 응답 수신")
                    android.util.Log.d("MainActivity", "응답 코드: $responseCode")
                    android.util.Log.d("MainActivity", "응답 본문: $responseBody")

                    if (response.isSuccessful) {
                        android.util.Log.d("MainActivity", "✅ FCM 토큰 서버 전송 성공")
                        true
                    } else {
                        android.util.Log.e("MainActivity", "❌ FCM 토큰 서버 전송 실패: $responseCode")
                        false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "FCM 토큰 전송 중 오류", e)
                false
            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // FCM 수신 통화 및 응급 전화 Intent 처리
        handleIncomingCallIntent(intent)
        handleEmergencyCallIntent(intent)
        handleNotificationIntent(intent)
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

    private fun handleEmergencyCallIntent(intent: Intent?) {
        if (intent?.getStringExtra("navigate_to") == "voip_call") {
            val targetName = intent.getStringExtra("target_name") ?: ""
            val targetPhone = intent.getStringExtra("target_phone") ?: ""
            val isCaller = intent.getBooleanExtra("is_caller", true)
            val userType = intent.getStringExtra("user_type") ?: "PATIENT"
            val receiverId = intent.getStringExtra("receiver_id") ?: ""
            val isEmergency = intent.getBooleanExtra("is_emergency", false)

            emergencyCallData = EmergencyCallData(
                targetName = targetName,
                targetPhone = targetPhone,
                isCaller = isCaller,
                userType = userType,
                receiverId = receiverId,
                isEmergency = isEmergency
            )
        }
    }

    private var notificationNavigation by mutableStateOf<String?>(null)

    private fun handleNotificationIntent(intent: Intent?) {
        val navigateTo = intent?.getStringExtra("navigate_to")
        if (navigateTo == "call_history") {
            notificationNavigation = "call_history"
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

/**
 * 응급 전화 데이터
 */
data class EmergencyCallData(
    val targetName: String,
    val targetPhone: String,
    val isCaller: Boolean,
    val userType: String,
    val receiverId: String,
    val isEmergency: Boolean
)
