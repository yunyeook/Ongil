package kr.co.ongil.data.datasource.websocket

import android.util.Log
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor() {

    companion object {
        private const val TAG = "WebSocketManager"
        private const val WS_URL = "wss://staging.on-gil.co.kr/api/ws/websocket"
        private const val CONNECTION_TIMEOUT_MS = 10_000L
    }

    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    fun connect(token: String) {
        if (stompClient?.isConnected == true) {
            Log.d(TAG, "Already connected")
            return
        }

        disconnect()

        try {
            stompClient = Stomp.over(
                Stomp.ConnectionProvider.JWS,
                WS_URL
            ).apply {
                withClientHeartbeat(10_000)
                withServerHeartbeat(10_000)
            }

            stompClient?.lifecycle()?.subscribe { event ->
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d(TAG, "✅ WebSocket OPENED")
                        _isConnected.value = true
                        _connectionError.value = null
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        Log.d(TAG, "WebSocket CLOSED")
                        _isConnected.value = false
                    }
                    LifecycleEvent.Type.ERROR -> {
                        Log.e(TAG, "❌ WebSocket ERROR: ${event.exception?.message}", event.exception)
                        _isConnected.value = false
                        _connectionError.value = event.exception?.message
                    }
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                        Log.w(TAG, "WebSocket FAILED_SERVER_HEARTBEAT")
                    }
                    else -> {
                        Log.d(TAG, "WebSocket event: ${event.type}")
                    }
                }
            }?.let { compositeDisposable.add(it) }

            val headers = arrayListOf(
                StompHeader("Authorization", "Bearer $token")
            )

            stompClient?.connect(headers)
            Log.d(TAG, "🔌 Connecting to SockJS STOMP: $WS_URL with auth header")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect WebSocket", e)
            _connectionError.value = e.message
        }
    }

    suspend fun waitForConnection(): Boolean {
        return withTimeoutOrNull(CONNECTION_TIMEOUT_MS) {
            isConnected.first { it }
            true
        } ?: false
    }

    fun subscribe(
        destination: String,
        onMessage: (String) -> Unit
    ): Disposable? {
        val client = stompClient ?: run {
            Log.w(TAG, "Cannot subscribe: stompClient is null")
            return null
        }

        return client.topic(destination).subscribe({ message ->
            Log.d(TAG, "Received message from $destination: ${message.payload}")
            onMessage(message.payload)
        }, { error ->
            Log.e(TAG, "Error subscribing to $destination", error)
        }).also { disposable ->
            if (disposable != null) {
                compositeDisposable.add(disposable)
                Log.d(TAG, "✓ Subscribed to $destination")
            } else {
                Log.e(TAG, "✗ Failed to subscribe to $destination (null disposable)")
            }
        }
    }

    fun send(destination: String, message: String) {
        val client = stompClient
        if (client == null || !client.isConnected) {
            Log.w(TAG, "Cannot send: not connected")
            return
        }

        client.send(destination, message).subscribe({
            Log.d(TAG, "Message sent to $destination")
        }, { error ->
            Log.e(TAG, "Error sending message to $destination", error)
        }).also { disposable ->
            if (disposable != null) {
                compositeDisposable.add(disposable)
            }
        }
    }

    fun disconnect() {
        compositeDisposable.clear()
        try {
            stompClient?.disconnect()
        } catch (e: Exception) {
            Log.w(TAG, "Error while disconnecting stomp", e)
        }
        stompClient = null
        _isConnected.value = false
        Log.d(TAG, "Disconnected from WebSocket")
    }

    fun cleanup() = disconnect()
}