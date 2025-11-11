package kr.co.ongil.data.websocket

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.data.model.websocket.GpsMessage
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GPS 위치 전송 WebSocket Manager
 */
@Singleton
class GpsWebSocketManager @Inject constructor(
    private val client: OkHttpClient,
    private val locationBus: LocationStreamBus
) {
    private val TAG = "GpsWebSocketManager"

    private var webSocket: WebSocket? = null
    private var scope: CoroutineScope? = null
    private var locationJob: Job? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private var isConnected = false

    /**
     * WebSocket 연결 시작
     */
    fun connect(baseUrl: String) {
        if (webSocket != null) {
            Log.w(TAG, "WebSocket already connected")
            return
        }

        // ws:// 또는 wss:// 프로토콜로 변환
        val wsUrl = baseUrl
            .replace("http://", "ws://")
            .replace("https://", "wss://")
            .trimEnd('/') + "/ws/gps"

        Log.d(TAG, "Connecting to WebSocket: $wsUrl")

        val request = Request.Builder()
            .url(wsUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
                isConnected = true
                startLocationTracking()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Received message: $text")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code - $reason")
                isConnected = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code - $reason")
                isConnected = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}", t)
                isConnected = false
            }
        })
    }

    /**
     * 위치 추적 시작 (LocationStreamBus 구독)
     */
    private fun startLocationTracking() {
        scope = CoroutineScope(Dispatchers.IO)

        locationJob = scope?.launch {
            locationBus.updates.collectLatest { location ->
                sendLocation(location.latitude, location.longitude)
            }
        }

        Log.d(TAG, "Location tracking started")
    }

    /**
     * GPS 위치 전송
     */
    private fun sendLocation(latitude: Double, longitude: Double) {
        if (!isConnected || webSocket == null) {
            Log.w(TAG, "WebSocket not connected, skipping location send")
            return
        }

        try {
            val timestamp = java.time.ZonedDateTime.now()
                .format(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME)

            val message = GpsMessage(
                latitude = latitude,
                longitude = longitude,
                timestamp = timestamp
            )

            val jsonString = json.encodeToString(message)
            val success = webSocket?.send(jsonString) ?: false

            if (success) {
                Log.d(TAG, "Sent GPS: ($latitude, $longitude)")
            } else {
                Log.w(TAG, "Failed to send GPS location")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending location: ${e.message}", e)
        }
    }

    /**
     * WebSocket 연결 해제
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")

        locationJob?.cancel()
        locationJob = null

        scope?.cancel()
        scope = null

        webSocket?.close(1000, "Navigation ended")
        webSocket = null

        isConnected = false
    }

    /**
     * 연결 상태 확인
     */
    fun isConnected(): Boolean = isConnected
}
