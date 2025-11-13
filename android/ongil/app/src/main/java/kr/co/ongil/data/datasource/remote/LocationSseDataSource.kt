package kr.co.ongil.data.datasource.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kr.co.ongil.BuildConfig
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.model.location.GpsUpdateEvent
import kr.co.ongil.data.model.location.NavigationUpdateEvent
import kr.co.ongil.data.model.location.SseEvent
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSource
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton



/**
 * SSE로 환자들의 실시간 위치를 수신하는 DataSource
 */
@Singleton
class LocationSseDataSource @Inject constructor(
    private val userDataStoreManager: UserDataStoreManager,
    private val json: Json
) {
    companion object {
        private const val TAG = "LocationSseDataSource"
    }

    private val sseClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.SECONDS) // 무한 대기
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * SSE 스트림 연결 (통합)
     * @return Flow<SseEvent> - SSE 이벤트 스트림
     */
    fun connectSseStream(): Flow<SseEvent> = callbackFlow {
        var response: Response? = null

        try {
            withContext(Dispatchers.IO) {
                // Access Token 가져오기
                val accessToken = userDataStoreManager.getAccessToken().first()

                if (accessToken.isNullOrBlank()) {
                    Log.e(TAG, "Access Token이 없습니다")
                    return@withContext
                }

                // SSE 요청
                val request = Request.Builder()
                    .url(BuildConfig.SSE_URL)
                    .header("Authorization", "Bearer $accessToken")
                    .header("Accept", "text/event-stream")
                    .get()
                    .build()

                Log.d(TAG, "SSE 연결 시작: ${BuildConfig.SSE_URL}")

                response = sseClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.e(TAG, "SSE 연결 실패: ${response.code}")
                    return@withContext
                }

                Log.d(TAG, "SSE 연결 성공")

                val source: BufferedSource? = response.body?.source()
                if (source == null) {
                    Log.e(TAG, "Response body가 null입니다")
                    return@withContext
                }

                var currentEvent: String? = null

                // SSE 스트림 읽기
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break

                    when {
                        line.startsWith("event:") -> {
                            currentEvent = line.removePrefix("event:").trim()
                        }
                        line.startsWith("data:") -> {
                            val data = line.removePrefix("data:").trim()

                            when (currentEvent) {
                                "connected" -> {
                                    Log.d(TAG, "SSE 연결 확인: $data")
                                    trySend(SseEvent.Connected)
                                }
                                "gps-update" -> {
                                    try {
                                        val gpsUpdate = json.decodeFromString<GpsUpdateEvent>(data)
                                        Log.d(TAG, "GPS 업데이트 수신: patientId=${gpsUpdate.patientId}, lat=${gpsUpdate.coordinate.latitude}, lon=${gpsUpdate.coordinate.longitude}")
                                        trySend(SseEvent.GpsUpdate(gpsUpdate))
                                    } catch (e: Exception) {
                                        Log.e(TAG, "GPS 데이터 파싱 실패: $data", e)
                                    }
                                }
                                "navigation-update" -> {
                                    try {
                                        val navUpdate = json.decodeFromString<NavigationUpdateEvent>(data)
                                        Log.d(TAG, "길찾기 업데이트 수신: patientId=${navUpdate.patientId}, status=${navUpdate.status}, route=${navUpdate.route != null}")
                                        trySend(SseEvent.NavigationUpdate(navUpdate))
                                    } catch (e: Exception) {
                                        Log.e(TAG, "길찾기 데이터 파싱 실패: $data", e)
                                    }
                                }
                                else -> {
                                    Log.d(TAG, "알 수 없는 이벤트: $currentEvent, data: $data")
                                }
                            }
                        }
                        line.isEmpty() -> {
                            // 빈 줄은 이벤트 구분자
                            currentEvent = null
                        }
                    }
                }

                Log.d(TAG, "SSE 스트림 종료")
            }
        } catch (e: Exception) {
            Log.e(TAG, "SSE 연결 중 오류", e)
        } finally {
            response?.close()
            close()
        }

        awaitClose {
            Log.d(TAG, "SSE Flow 닫힘")
            response?.close()
        }
    }
}
