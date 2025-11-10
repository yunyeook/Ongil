package kr.co.ongil.presentation.ui.call

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.ongil.common.location.LocationPoint
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.core.webrtc.WebRtcCallClient
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.datasource.websocket.VoipSignalingService
import kr.co.ongil.data.model.call.CallStartLocationRequest
import kr.co.ongil.data.model.call.TurnCredentialsDto
import kr.co.ongil.data.model.call.VoipCallDto
import kr.co.ongil.data.model.websocket.SignalMessage
import kr.co.ongil.domain.repository.CallRepository
import org.webrtc.PeerConnection
import java.time.Instant

@HiltViewModel
class VoipCallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val locationStreamBus: LocationStreamBus,
    private val application: Application,
    private val webRtcCallClient: WebRtcCallClient,
    private val voipSignalingService: VoipSignalingService,
    private val userDataStoreManager: UserDataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoipCallUiState())
    val uiState: StateFlow<VoipCallUiState> = _uiState.asStateFlow()

    private var currentCall: VoipCallDto? = null
    private var currentUserId: Long? = null

    init {
        // 위치 스트림 구독
        viewModelScope.launch {
            locationStreamBus.updates.collect { point ->
                _uiState.update {
                    it.copy(
                        currentLocation = "위도: ${point.latitude}, 경도: ${point.longitude}, 정확도: ${point.accuracyMeters}m"
                    )
                }
            }
        }

        // 로그인 사용자 ID 구독
        viewModelScope.launch {
            userDataStoreManager.getLoginUserId().collect { id ->
                currentUserId = id?.toLongOrNull()
                Log.d(TAG, "CurrentUserId = $currentUserId")
            }
        }

        // 시그널링 메시지 구독
        viewModelScope.launch {
            voipSignalingService.signalingMessages.collect { signal ->
                handleSignalMessage(signal)
            }
        }
    }

    // =========================================================
    // 📞 발신자 플로우
    // =========================================================

    fun startVoipCall(
        receiverId: Long,
        userType: String,
        callType: String = "NORMAL"
    ) {
        Log.d(TAG, "=== [CALLER] startVoipCall: to=$receiverId, userType=$userType, type=$callType")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, error = null) }

            try {
                // 1. 통화 세션 생성
                val call = callRepository.createVoipCall(receiverId, callType).getOrThrow()
                currentCall = call

                Log.d(TAG, "createVoipCall success: callId=${call.id}, sessionId=${call.sessionId}")

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        call = call,
                        message = "통화 생성 완료"
                    )
                }

                // 2. 환자인 경우 시작 위치 전송
                if (userType == "PATIENT") {
                    sendStartLocationOnce(call.id)
                }

                // 3. WebSocket 연결
                val token = userDataStoreManager.getAccessToken().first()
                if (token.isNullOrBlank()) {
                    Log.e(TAG, "Access token is null")
                    _uiState.update { it.copy(error = "인증 토큰 없음") }
                    return@launch
                }

                val connected = voipSignalingService.connectAndWait(token)
                if (!connected) {
                    Log.e(TAG, "WebSocket 연결 실패")
                    _uiState.update { it.copy(error = "시그널링 서버 연결 실패") }
                    return@launch
                }
                Log.d(TAG, "✓ WebSocket 연결 성공")

                // 3-1. 통화방 구독
                val subscribed = voipSignalingService.subscribeToCall(call.id)
                if (!subscribed) {
                    Log.e(TAG, "통화방 구독 실패")
                    _uiState.update { it.copy(error = "통화방 구독 실패") }
                    return@launch
                }
                Log.d(TAG, "✓ Subscribed to /topic/calls/${call.id}")

                // 4. TURN 조회 & WebRTC 초기화
                val turn = callRepository.getTurnCredentials().getOrThrow()
                Log.d(TAG, "getTurnCredentials success: uris=${turn.uris}")

                val iceServers = turn.toIceServers()
                webRtcCallClient.init(iceServers)

                // 4-1. PeerConnection 상태 모니터링 설정
                setupPeerConnectionStateMonitoring()

                // 4-2. Offer 생성 전 사용자 ID 확인
                val fromUserId = currentUserId
                if (fromUserId == null) {
                    Log.e(TAG, "currentUserId is null → Offer 전송 불가")
                    _uiState.update { it.copy(error = "로그인 정보 없음 (UserId)") }
                    return@launch
                }

                // 4-3. ICE candidate 리스너 설정
                setupIceCandidateListener(call.id, call.sessionId, fromUserId, receiverId)

                // 5. Offer 생성 및 전송

                webRtcCallClient.createOffer { sdp ->
                    Log.d(TAG, "Offer SDP created")

                    voipSignalingService.sendOffer(
                        callId = call.id,
                        sessionId = call.sessionId,
                        fromUserId = fromUserId,
                        toUserId = receiverId,
                        sdp = sdp.description
                    )

                    Log.d(TAG, "Offer sent via signaling: callId=${call.id}, sessionId=${call.sessionId}")
                    _uiState.update {
                        it.copy(message = "통화 요청 전송 완료 (Offer)")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "startVoipCall failed: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "통화 시작 실패"
                    )
                }
            }
        }
    }

    // =========================================================
    // 📲 수신자 플로우
    // =========================================================

    fun loadIncomingCall(callId: Long) {
        Log.d(TAG, "loadIncomingCall: callId=$callId")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, error = null) }

            callRepository.getVoipCall(callId)
                .onSuccess { call ->
                    currentCall = call
                    Log.d(TAG, "getVoipCall success: status=${call.status}")

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            call = call,
                            message = "통화 조회 성공"
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "getVoipCall failed: ${e.message}", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "통화 정보 조회 실패"
                        )
                    }
                }
        }
    }

    fun acceptCall(userType: String) {
        val callId = currentCall?.id ?: run {
            Log.w(TAG, "acceptCall: currentCall is null")
            return
        }

        Log.d(TAG, "=== [CALLEE] acceptCall: callId=$callId, userType=$userType")

        viewModelScope.launch {
            callRepository.updateVoipCallStatus(callId, "CONNECTED")
                .onSuccess { updated ->
                    currentCall = updated
                    Log.d(TAG, "acceptCall: status=${updated.status}")

                    _uiState.update {
                        it.copy(
                            call = updated,
                            message = "통화 연결됨"
                        )
                    }

                    if (userType == "PATIENT") {
                        sendStartLocationOnce(callId)
                    }

                    setupWebRtcAsCallee()
                }
                .onFailure { e ->
                    Log.e(TAG, "acceptCall failed: ${e.message}", e)
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "통화 수락 실패"
                        )
                    }
                }
        }
    }

    fun endCall() {
        val call = currentCall ?: run {
            Log.w(TAG, "endCall: currentCall is null")
            return
        }

        if (call.status == "ENDED") {
            _uiState.update { it.copy(message = "이미 종료된 통화입니다.") }
            return
        }

        Log.d(TAG, "=== endCall: callId=${call.id}, status=${call.status}")

        viewModelScope.launch {
            callRepository.updateVoipCallStatus(call.id, "ENDED")
                .onSuccess { updated ->
                    currentCall = updated
                    webRtcCallClient.endCall()
                    voipSignalingService.disconnect()

                    _uiState.update {
                        it.copy(
                            call = updated,
                            message = "통화 종료됨"
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "endCall failed: ${e.message}", e)
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "통화 종료 실패"
                        )
                    }
                }
        }
    }

    // =========================================================
    // 🔁 시그널링 수신 처리
    // =========================================================

    /**
     * ICE candidate 리스너 설정
     * WebRTC에서 로컬 ICE candidate가 생성되면 시그널링 서버로 전송
     */
    private fun setupIceCandidateListener(
        callId: Long,
        sessionId: String?,
        fromUserId: Long,
        toUserId: Long
    ) {
        webRtcCallClient.setOnLocalIceCandidateListener { iceCandidate ->
            Log.d(TAG, "Local ICE candidate generated: ${iceCandidate.sdp}")

            voipSignalingService.sendIceCandidate(
                callId = callId,
                sessionId = sessionId,
                fromUserId = fromUserId,
                toUserId = toUserId,
                candidate = iceCandidate.sdp,
                sdpMid = iceCandidate.sdpMid,
                sdpMLineIndex = iceCandidate.sdpMLineIndex
            )

            Log.d(TAG, "✓ ICE candidate sent to remote peer")
        }
    }

    /**
     * PeerConnection 상태 모니터링 설정
     * 실제 WebRTC 연결이 CONNECTED 상태가 되면 서버에 통화 상태 업데이트
     */
    private fun setupPeerConnectionStateMonitoring() {
        webRtcCallClient.setOnPeerConnectionStateChangeListener { state ->
            Log.d(TAG, "🔗 PeerConnection state changed: $state")

            when (state) {
                PeerConnection.PeerConnectionState.CONNECTED -> {
                    Log.d(TAG, "✅ WebRTC 연결 성공!")
                    _uiState.update {
                        it.copy(message = "✅ WebRTC 연결 성공")
                    }
                }
                PeerConnection.PeerConnectionState.FAILED -> {
                    Log.e(TAG, "❌ WebRTC 연결 실패")
                    _uiState.update {
                        it.copy(error = "WebRTC 연결 실패")
                    }
                }
                PeerConnection.PeerConnectionState.DISCONNECTED -> {
                    Log.w(TAG, "⚠️ WebRTC 연결 끊김")
                    _uiState.update {
                        it.copy(message = "연결이 끊겼습니다")
                    }
                }
                else -> {
                    // NEW, CONNECTING, CLOSED 등
                }
            }
        }
    }

    private fun handleSignalMessage(signal: SignalMessage) {
        Log.d(TAG, "Received signal: type=${signal.type}, callId=${signal.callId}")

        when (signal.type) {
            "INCOMING" -> {
                Log.d(TAG, "INCOMING signal (이미 FCM에서 화면 전환 처리됨)")
            }
            "OFFER" -> {
                signal.sdp?.let { sdp ->
                    val callId = signal.callId ?: return
                    val senderId = signal.senderId ?: return
                    handleOffer(sdp, callId, senderId)
                }
            }
            "ANSWER" -> {
                signal.sdp?.let { sdp ->
                    handleAnswer(sdp)
                }
            }
            "ICE" -> {
                signal.candidate?.let { candidate ->
                    handleIceCandidate(
                        candidate = candidate,
                        sdpMid = signal.sdpMid,
                        sdpMLineIndex = signal.sdpMLineIndex ?: 0
                    )
                }
            }
            "HANGUP" -> {
                Log.d(TAG, "Remote user hung up: ${signal.reason}")
                _uiState.update {
                    it.copy(message = "상대방이 통화를 종료했습니다.")
                }
                webRtcCallClient.endCall()
                voipSignalingService.disconnect()
            }
        }
    }

    private fun handleOffer(sdp: String, callId: Long, senderId: Long) {
        Log.d(TAG, "Handling OFFER from $senderId for callId=$callId")

        // 1. Remote Offer SDP 설정
        webRtcCallClient.setRemoteDescription(
            type = org.webrtc.SessionDescription.Type.OFFER,
            sdp = sdp
        )
        Log.d(TAG, "Remote Offer SDP set")

        // 2. Answer 생성 및 전송
        webRtcCallClient.createAnswer { answerSdp ->
            val myUserId = currentUserId
            if (myUserId == null) {
                Log.e(TAG, "currentUserId is null, cannot send Answer")
                return@createAnswer
            }

            voipSignalingService.sendAnswer(
                callId = callId,
                sessionId = currentCall?.sessionId,
                fromUserId = myUserId,
                toUserId = senderId,
                sdp = answerSdp.description
            )
            Log.d(TAG, "Answer sent to caller $senderId")
        }
    }

    private fun handleAnswer(sdp: String) {
        Log.d(TAG, "Handling ANSWER")

        // 1. Remote Answer SDP 설정
        webRtcCallClient.setRemoteDescription(
            type = org.webrtc.SessionDescription.Type.ANSWER,
            sdp = sdp
        )
        Log.d(TAG, "Remote Answer SDP set")

        // 2. 발신자 측: Answer를 받았으므로 통화 상태를 CONNECTED로 업데이트
        val call = currentCall
        if (call == null) {
            Log.w(TAG, "currentCall is null, cannot update status to CONNECTED")
            return
        }

        viewModelScope.launch {
            callRepository.updateVoipCallStatus(call.id, "CONNECTED")
                .onSuccess { updated ->
                    currentCall = updated
                    Log.d(TAG, "✓ [CALLER] Call status updated to CONNECTED")
                    _uiState.update {
                        it.copy(
                            call = updated,
                            message = "통화 연결됨"
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to update call status: ${e.message}", e)
                }
        }
    }

    private fun handleIceCandidate(candidate: String, sdpMid: String?, sdpMLineIndex: Int) {
        Log.d(TAG, "Handling ICE candidate")
        webRtcCallClient.addRemoteIceCandidate(sdpMid, sdpMLineIndex, candidate)
    }

    // =========================================================
    // 🔧 수신자 WebRTC 초기화
    // =========================================================

    private fun setupWebRtcAsCallee() {
        val call = currentCall ?: run {
            Log.w(TAG, "setupWebRtcAsCallee: currentCall is null")
            return
        }

        viewModelScope.launch {
            runCatching { callRepository.getTurnCredentials().getOrThrow() }
                .onSuccess { turn ->
                    val iceServers = turn.toIceServers()
                    webRtcCallClient.init(iceServers)
                    Log.d(TAG, "[CALLEE] WebRTC init 완료")

                    // PeerConnection 상태 모니터링 설정
                    setupPeerConnectionStateMonitoring()

                    // ICE candidate 리스너 설정
                    val myUserId = currentUserId
                    if (myUserId != null) {
                        setupIceCandidateListener(
                            call.id,
                            call.sessionId,
                            myUserId,
                            call.callerId  // 수신자 입장에서 상대방은 발신자
                        )
                    } else {
                        Log.w(TAG, "[CALLEE] currentUserId is null, ICE candidate 전송 불가")
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "setupWebRtcAsCallee TURN 실패: ${e.message}", e)
                    _uiState.update {
                        it.copy(error = "TURN 정보 조회 실패: ${e.message}")
                    }
                }
        }
    }

    // =========================================================
    // 📍 위치 관련
    // =========================================================

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            if (!hasLocationPermission()) {
                _uiState.update {
                    it.copy(error = "⚠️ 위치 권한이 필요합니다. 앱 설정에서 위치 권한을 허용해주세요.")
                }
                return@launch
            }

            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(application)
                val lastLocation = lastLocationOrNull(fusedClient)

                if (lastLocation != null) {
                    val point = LocationPoint(
                        latitude = lastLocation.latitude,
                        longitude = lastLocation.longitude,
                        accuracyMeters = lastLocation.accuracy,
                        bearing = lastLocation.bearing,
                        speedMps = lastLocation.speed,
                        timeMillis = lastLocation.time
                    )
                    _uiState.update {
                        it.copy(
                            currentLocation = "위도: ${point.latitude}, 경도: ${point.longitude}, 정확도: ${point.accuracyMeters}m",
                            message = "✓ 위치 가져오기 성공"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(error = "위치 정보가 없습니다. GPS를 켜고 잠시 기다려주세요.")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "위치 가져오기 실패: ${e.message}")
                }
            }
        }
    }

    private suspend fun lastLocationOrNull(client: com.google.android.gms.location.FusedLocationProviderClient) =
        try {
            client.lastLocation.await()
        } catch (e: Exception) {
            Log.e(TAG, "lastLocation 실패", e)
            null
        }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    private fun sendStartLocationOnce(callId: Long) {
        viewModelScope.launch {
            if (!hasLocationPermission()) {
                _uiState.update {
                    it.copy(error = "⚠️ 위치 권한이 필요합니다. 앱 설정에서 위치 권한을 허용해주세요.")
                }
                return@launch
            }

            var point = locationStreamBus.lastValue

            if (point == null) {
                try {
                    val fusedClient =
                        LocationServices.getFusedLocationProviderClient(application)
                    val lastLocation = lastLocationOrNull(fusedClient)
                    if (lastLocation != null) {
                        point = LocationPoint(
                            latitude = lastLocation.latitude,
                            longitude = lastLocation.longitude,
                            accuracyMeters = lastLocation.accuracy,
                            bearing = lastLocation.bearing,
                            speedMps = lastLocation.speed,
                            timeMillis = lastLocation.time
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "FusedLocation fallback 실패", e)
                }
            }

            if (point == null) {
                _uiState.update {
                    it.copy(error = "⚠️ 위치 정보를 가져올 수 없습니다. 위치 권한과 GPS를 확인하세요.")
                }
                return@launch
            }

            val request = CallStartLocationRequest(
                latitude = point.latitude,
                longitude = point.longitude,
                source = "GPS",
                accuracy = point.accuracyMeters?.toDouble(),
                timestamp = Instant.ofEpochMilli(point.timeMillis).toString()
            )

            callRepository.sendVoipCallStartLocation(callId, request)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            message = "✓ 위치 전송 완료 (${point.latitude}, ${point.longitude})"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            error = "✗ 위치 전송 실패: ${e.message}"
                        )
                    }
                }
        }
    }

    // =========================================================

    fun initIncomingCall(callId: Long, sessionId: String?) {
        Log.d(TAG, "===== FCM 수신 통화 초기화 ===== callId=$callId, sessionId=$sessionId")

        viewModelScope.launch {
            try {
                val token = userDataStoreManager.getAccessToken().first()
                if (token.isNullOrBlank()) {
                    Log.e(TAG, "Access token is null")
                    _uiState.update { it.copy(error = "인증 토큰 없음") }
                    return@launch
                }

                val connected = voipSignalingService.connectAndWait(token)
                if (!connected) {
                    Log.e(TAG, "WebSocket 연결 실패")
                    _uiState.update { it.copy(error = "시그널링 서버 연결 실패") }
                    return@launch
                }
                Log.d(TAG, "✓ WebSocket 연결 성공")

                val subscribed = voipSignalingService.subscribeToCall(callId)
                if (!subscribed) {
                    Log.e(TAG, "통화방 구독 실패")
                    _uiState.update { it.copy(error = "통화방 구독 실패") }
                    return@launch
                }
                Log.d(TAG, "✓ Subscribed to /topic/calls/$callId")

                loadIncomingCall(callId)

            } catch (e: Exception) {
                Log.e(TAG, "수신 통화 초기화 실패", e)
                _uiState.update {
                    it.copy(error = "통화 초기화 실패: ${e.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "VoipCallViewModel onCleared")
        voipSignalingService.disconnect()
        webRtcCallClient.endCall()
    }

    companion object {
        private const val TAG = "VoipCallViewModel"
    }
}

/** TURN DTO → ICE 서버 변환 */
private fun TurnCredentialsDto.toIceServers(): List<PeerConnection.IceServer> =
    uris.map {
        PeerConnection.IceServer.builder(it)
            .setUsername(username)
            .setPassword(credential)
            .createIceServer()
    }