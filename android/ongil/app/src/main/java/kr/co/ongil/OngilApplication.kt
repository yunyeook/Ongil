package kr.co.ongil

import android.app.Application
import android.util.Log
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@HiltAndroidApp
class OngilApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 앱 시작 시 토큰 동기화
        CoroutineScope(Dispatchers.IO).launch {
            syncFcmTokenOnAppStart()
        }
    }

    private suspend fun syncFcmTokenOnAppStart() {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                UserDataStoreManagerEntryPoint::class.java
            )
            val userDataStoreManager = entryPoint.userDataStoreManager()

            val accessToken = userDataStoreManager.getAccessToken().firstOrNull()

            if (accessToken != null) {
                // 현재 FCM 토큰 가져오기
                val fcmToken = suspendCancellableCoroutine<String?> { continuation ->
                    FirebaseMessaging.getInstance().token
                        .addOnSuccessListener { token ->
                            Log.d("FCM_DEBUG", "📱 현재 FCM 토큰: ${token?.take(20)}...")
                            continuation.resumeWith(Result.success(token))
                        }
                        .addOnFailureListener {
                            continuation.resumeWith(Result.success(null))
                        }
                }

                if (fcmToken != null) {
                    // 👇 저장된 토큰과 비교
                    val savedToken = userDataStoreManager.getFcmToken().firstOrNull()
                    Log.d("FCM_DEBUG", "💾 저장된 FCM 토큰: ${savedToken?.take(20)}...")

                    if (fcmToken == savedToken) {
                        Log.d("FCM_DEBUG", "✅ 토큰 동일 - 서버 전송 스킵")
                        return
                    }

                    Log.d("FCM_DEBUG", "🔄 토큰 변경됨 - 서버 전송 시도")

                    // 토큰이 다른 경우에만 서버 전송
                    val success = sendTokenToServer(fcmToken, accessToken)
                    if (success) {
                        userDataStoreManager.saveFcmToken(fcmToken)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FCM", "앱 시작 시 토큰 동기화 실패", e)
        }
    }

    private suspend fun sendTokenToServer(fcmToken: String, accessToken: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val json = JSONObject().apply { put("token", fcmToken) }
                val body = json.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("${BuildConfig.BASE_URL}api/v1/fcm/register")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    when {
                        response.isSuccessful -> {
                            Log.d("FCM", "✅ 앱 시작 시 토큰 전송 성공")
                            true
                        }

                        response.code in 400..599 -> {
                            Log.w("FCM", "⚠️ 토큰 전송 에러: ${response.code} - 무시")
                            false
                        }

                        else -> {
                            Log.e("FCM", "❌ 토큰 전송 실패: ${response.code}")
                            false
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FCM", "토큰 전송 중 오류", e)
                false
            }
        }
    }

    // EntryPoint 인터페이스 추가 (같은 파일 또는 별도 파일)
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UserDataStoreManagerEntryPoint {
        fun userDataStoreManager(): kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
    }
}