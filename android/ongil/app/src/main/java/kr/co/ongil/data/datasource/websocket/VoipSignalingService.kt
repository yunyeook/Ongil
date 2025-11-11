package kr.co.ongil.data.datasource.websocket

import android.util.Log
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.co.ongil.data.model.websocket.SignalMessage
import kr.co.ongil.data.model.websocket.SignalingType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoipSignalingService @Inject constructor(
    private val webSocketManager: WebSocketManager,
    private val json: Json
) {

    companion object {
        private const val TAG = "VoipSignalingService"
        private fun topicCall(callId: Long) = "/topic/calls/$callId"
        private fun destCallSignal(callId: Long) = "/app/calls/$callId/signal"
    }

    private val callSubscriptions = mutableMapOf<Long, Disposable>()

    private val _signalingMessages = MutableSharedFlow<SignalMessage>()
    val signalingMessages: SharedFlow<SignalMessage> = _signalingMessages.asSharedFlow()

    suspend fun connectAndWait(token: String): Boolean {
        Log.d(TAG, "try connectAndWait")
        webSocketManager.connect(token)

        val connected = webSocketManager.waitForConnection()
        if (connected) Log.d(TAG, "✓ WebSocket connected")
        else Log.e(TAG, "✗ WebSocket connect timeout")
        return connected
    }

    fun subscribeToCall(callId: Long): Boolean {
        if (callSubscriptions.containsKey(callId)) {
            Log.d(TAG, "Already subscribed to call $callId")
            return true
        }

        val topic = topicCall(callId)
        val disposable = webSocketManager.subscribe(topic) { message ->
            handleSignalMessage(message)
        }

        return if (disposable != null) {
            callSubscriptions[callId] = disposable
            Log.d(TAG, "✓ Subscribed to $topic")
            true
        } else {
            Log.e(TAG, "✗ Failed to subscribe to $topic")
            false
        }
    }

    fun unsubscribeFromCall(callId: Long) {
        callSubscriptions[callId]?.dispose()
        callSubscriptions.remove(callId)
        Log.d(TAG, "Unsubscribed from call $callId")
    }

    fun sendOffer(
        callId: Long,
        sessionId: String?,
        fromUserId: Long,
        toUserId: Long,
        sdp: String
    ) {
        val msg = SignalMessage(
            type = SignalingType.OFFER.name,
            callId = callId,
            sessionId = sessionId,
            senderId = fromUserId,
            receiverId = toUserId,
            sdp = sdp
        )
        sendSignal(callId, msg, "OFFER")
    }

    fun sendAnswer(
        callId: Long,
        sessionId: String?,
        fromUserId: Long,
        toUserId: Long,
        sdp: String
    ) {
        val msg = SignalMessage(
            type = SignalingType.ANSWER.name,
            callId = callId,
            sessionId = sessionId,
            senderId = fromUserId,
            receiverId = toUserId,
            sdp = sdp
        )
        sendSignal(callId, msg, "ANSWER")
    }

    fun sendIceCandidate(
        callId: Long,
        sessionId: String?,
        fromUserId: Long,
        toUserId: Long,
        candidate: String,
        sdpMid: String?,
        sdpMLineIndex: Int
    ) {
        val msg = SignalMessage(
            type = SignalingType.ICE.name,
            callId = callId,
            sessionId = sessionId,
            senderId = fromUserId,
            receiverId = toUserId,
            candidate = candidate,
            sdpMid = sdpMid,
            sdpMLineIndex = sdpMLineIndex
        )
        sendSignal(callId, msg, "ICE")
    }

    fun sendHangup(
        callId: Long,
        sessionId: String?,
        fromUserId: Long,
        toUserId: Long
    ) {
        val msg = SignalMessage(
            type = SignalingType.HANGUP.name,
            callId = callId,
            sessionId = sessionId,
            senderId = fromUserId,
            receiverId = toUserId
        )
        sendSignal(callId, msg, "HANGUP")
    }

    private fun sendSignal(callId: Long, msg: SignalMessage, logType: String) {
        try {
            val jsonMessage = json.encodeToString(msg)
            val dest = destCallSignal(callId)
            webSocketManager.send(dest, jsonMessage)
            Log.d(TAG, "Sent $logType to $dest")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending $logType", e)
        }
    }

    private fun handleSignalMessage(message: String) {
        try {
            val signal = json.decodeFromString<SignalMessage>(message)

            when (signal.type) {
                SignalingType.INCOMING.name ->
                    Log.d(TAG, "Received INCOMING: callId=${signal.callId}, caller=${signal.callerName}")
                SignalingType.OFFER.name ->
                    Log.d(TAG, "Received OFFER for call ${signal.callId}")
                SignalingType.ANSWER.name ->
                    Log.d(TAG, "Received ANSWER for call ${signal.callId}")
                SignalingType.ICE.name ->
                    Log.d(TAG, "Received ICE for call ${signal.callId}")
                SignalingType.HANGUP.name ->
                    Log.d(TAG, "Received HANGUP for call ${signal.callId}")
                else ->
                    Log.w(TAG, "Unknown signal type: ${signal.type}")
            }

            runBlocking {
                _signalingMessages.emit(signal)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing signal message", e)
        }
    }

    fun disconnect() {
        callSubscriptions.values.forEach { it.dispose() }
        callSubscriptions.clear()
        webSocketManager.disconnect()
        Log.d(TAG, "VoipSignalingService disconnected")
    }

    fun cleanup() = disconnect()
}