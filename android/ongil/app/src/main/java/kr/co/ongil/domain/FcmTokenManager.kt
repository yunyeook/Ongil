package kr.co.ongil.domain

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.ongil.BuildConfig
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val userDataStoreManager: UserDataStoreManager
) {
    // ✅ Application 생명주기와 동일한 독립적인 Scope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun syncTokenAfterLogin() {
        Log.d("FCM_MANAGER", "🚀 FCM 토큰 동기화 시작 (독립 Scope)")
        scope.launch {
            try {
                syncToken()
            } catch (e: Exception) {
                Log.e("FCM_MANAGER", "토큰 동기화 실패", e)
            }
        }
    }

    private suspend fun syncToken() {
        try {
            val isSynced = userDataStoreManager.isFcmTokenSynced().firstOrNull() ?: false
            if (isSynced) {
                Log.d("FCM_MANAGER", "✅ 이미 동기화됨, 스킵")
                return
            }

            var fcmToken = userDataStoreManager.getFcmToken().firstOrNull()

            if (fcmToken.isNullOrBlank()) {
                Log.d("FCM_MANAGER", "로컬 토큰 없음, Firebase에서 직접 조회")
                fcmToken = try {
                    Tasks.await(FirebaseMessaging.getInstance().token)
                } catch (e: Exception) {
                    Log.e("FCM_MANAGER", "Firebase 토큰 조회 실패", e)
                    null
                }

                if (!fcmToken.isNullOrBlank()) {
                    userDataStoreManager.saveFcmToken(fcmToken)
                }
            }

            if (fcmToken.isNullOrBlank()) {
                Log.e("FCM_MANAGER", "❌ FCM 토큰을 가져올 수 없음")
                return
            }

            Log.d("FCM_MANAGER", "🚀 서버로 토큰 전송 시도: ${fcmToken.take(20)}...")

            val success = sendTokenToBackend(fcmToken)

            if (success) {
                userDataStoreManager.setFcmTokenSynced(true)
                Log.d("FCM_MANAGER", "✅ FCM 토큰 처리 완료")
            }

        } catch (e: Exception) {
            Log.e("FCM_MANAGER", "❌ FCM 토큰 처리 실패", e)
        }
    }

    private suspend fun sendTokenToBackend(token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val accessToken = userDataStoreManager.getAccessToken().firstOrNull()
                if (accessToken == null) {
                    Log.e("FCM_MANAGER", "AccessToken이 없어서 FCM 토큰 전송 실패")
                    return@withContext false
                }

                val client = OkHttpClient()
                val json = JSONObject().apply { put("token", token) }
                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("${BuildConfig.BASE_URL}api/v1/fcm/register")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(body)
                    .build()

                Log.d("FCM_MANAGER", "📡 HTTP 요청 전송 중...")

                client.newCall(request).execute().use { response ->
                    Log.d("FCM_MANAGER", "📡 HTTP 응답 수신: ${response.code}")

                    if (response.isSuccessful) {
                        Log.d("FCM_MANAGER", "✅ FCM 토큰 서버 전송 성공")
                        true
                    } else {
                        val errorBody = response.body?.string()
                        Log.e("FCM_MANAGER", "❌ 토큰 서버 응답 오류: ${response.code}, body: $errorBody")
                        false
                    }
                }
            } catch (e: Exception) {
                Log.e("FCM_MANAGER", "❌ 토큰 전송 중 오류", e)
                false
            }
        }
    }
}