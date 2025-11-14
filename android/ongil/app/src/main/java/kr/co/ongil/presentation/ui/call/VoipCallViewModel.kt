package kr.co.ongil.presentation.ui.call

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.core.webrtc.WebRtcCallClient
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.datasource.websocket.VoipSignalingService
import kr.co.ongil.data.model.call.TurnCredentialsDto
import kr.co.ongil.data.model.call.VoipCallDto
import kr.co.ongil.data.model.websocket.SignalMessage
import kr.co.ongil.domain.repository.CallRepository
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription

@HiltViewModel
class VoipCallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val webRtcCallClient: WebRtcCallClient,
    private val voipSignalingService: VoipSignalingService,
    private val userDataStoreManager: UserDataStoreManager
) : ViewModel() {
    private val pendingIceCandidates = mutableListOf<IceCandidateInfo>()
    private var isRemoteDescriptionSet = false
    private var isWebRtcInitialized = false

    data class IceCandidateInfo(
        val candidate: String,
        val sdpMid: String?,
        val sdpMLineIndex: Int
    )
    private val _uiState = MutableStateFlow(VoipCallUiState())
    val uiState: StateFlow<VoipCallUiState> = _uiState.asStateFlow()

    private var currentCall: VoipCallDto? = null
    private var currentUserId: Long? = null
    private var callTimerJob: Job? = null

    init {
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

                // 2. WebSocket 연결
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

                // 3. 통화방 구독
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
                val fromUserId = currentUserId ?: return@launch

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

                    Log.d(
                        TAG,
                        "Offer sent via signaling: callId=${call.id}, sessionId=${call.sessionId}"
                    )
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
        val call = currentCall ?: run {
            Log.w(TAG, "acceptCall: currentCall is null")
            return
        }

        Log.d(TAG, "=== [CALLEE] acceptCall: callId=${call.id}, userType=$userType")

        viewModelScope.launch {
            // 1. 먼저 발신자에게 ACCEPT 시그널 전송
            val myUserId = currentUserId
            val callerId = call.callerId

            if (myUserId != null && callerId != null) {
                voipSignalingService.sendAccept(
                    callId = call.id,
                    sessionId = call.sessionId,
                    fromUserId = myUserId,
                    toUserId = callerId
                )
                Log.d(TAG, "✓ ACCEPT signal sent to caller")
            } else {
                Log.w(TAG, "Cannot send ACCEPT: myUserId=$myUserId, callerId=$callerId")
            }

            // 2. 서버에 상태 업데이트
            callRepository.updateVoipCallStatus(call.id, "CONNECTED")
                .onSuccess { updated ->
                    currentCall = updated
                    Log.d(TAG, "acceptCall: status=${updated.status}")

                    _uiState.update {
                        it.copy(
                            call = updated,
                            message = "통화 연결됨"
                        )
                    }

                    // ⏱️ 타이머 시작
                    startTimer()

                    if (callerId != null) {
                        ensureWebRtcInitializedForCallee(call.id, callerId)
                    }
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
            val call = uiState.value.call ?: return
            val myId = currentUserId
            val callerId = call.callerId
            val receiverId = call.receiverId

            Log.w(TAG, "endCall: currentCall is null")
            return
        }

        if (call.status == "ENDED") {
            _uiState.update { it.copy(message = "이미 종료된 통화입니다.") }
            return
        }

        Log.d(TAG, "=== endCall: callId=${call.id}, status=${call.status}")

        // ⏱️ 타이머 정지
        stopTimer()

        viewModelScope.launch {
            // 1. 먼저 상대방에게 HANGUP 시그널 전송
            val myUserId = currentUserId
            val callerId = call.callerId
            val receiverId = call.receiverId

            if (myUserId != null && callerId != null && receiverId != null) {
                val toUserId = if (myUserId == callerId) receiverId else callerId
                voipSignalingService.sendHangup(
                    callId = call.id,
                    sessionId = call.sessionId,
                    fromUserId = myUserId,
                    toUserId = toUserId
                )
                Log.d(TAG, "✓ HANGUP signal sent to remote peer")
            } else {
                Log.w(
                    TAG,
                    "Cannot send HANGUP: myUserId=$myUserId, callerId=$callerId, receiverId=$receiverId"
                )
            }

            // 2. 서버에 상태 업데이트
            callRepository.updateVoipCallStatus(call.id, "ENDED")
                .onSuccess { updated ->
                    currentCall = updated
                    Log.d(TAG, "✓ Call status updated to ENDED")
                    _uiState.update {
                        it.copy(
                            call = updated,
                            message = "통화 종료됨"
                        )
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "endCall failed: ${e.message}, but cleaning up anyway", e)
                    // 409 에러 등으로 이미 종료된 경우에도 로컬 상태 업데이트
                    currentCall = call.copy(status = "ENDED")
                    _uiState.update {
                        it.copy(
                            call = call.copy(status = "ENDED"),
                            message = "통화 종료됨"
                        )
                    }
                }

            // 3. 성공/실패 관계없이 항상 리소스 정리
            webRtcCallClient.endCall()
            voipSignalingService.disconnect()
            Log.d(TAG, "✓ WebRTC and signaling cleaned up")
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
                    val callId = signal.callId ?: run {
                        Log.e(TAG, "OFFER received but callId is null")
                        return
                    }

                    // ✅ currentCall에서 senderId 가져오기 (이미 loadIncomingCall에서 로드됨)
                    val call = currentCall
                    if (call == null || call.id != callId) {
                        Log.e(TAG, "Cannot process OFFER: currentCall is null or mismatched")
                        return
                    }

                    val senderId = call.callerId ?: run {
                        Log.e(TAG, "Cannot process OFFER: callerId is null in currentCall")
                        return
                    }

                    Log.d(TAG, "✅ Processing OFFER from senderId=$senderId")
                    handleOffer(sdp, callId, senderId)
                } ?: run {
                    Log.e(TAG, "OFFER received but SDP is null")
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
            "ACCEPT" -> {
                Log.d(TAG, "✅ Remote user accepted the call")
                val call = currentCall ?: run {
                    Log.w(TAG, "currentCall is null when receiving ACCEPT")
                    return
                }

                Log.d(
                    TAG,
                    "[CALLER] Received ACCEPT for callId=${call.id}, current status=${call.status}"
                )

                viewModelScope.launch {
                    callRepository.updateVoipCallStatus(call.id, "CONNECTED")
                        .onSuccess { updated ->
                            currentCall = updated
                            Log.d(TAG, "✓ [CALLER] Call status updated to CONNECTED after ACCEPT")
                            Log.d(
                                TAG,
                                "✓ [CALLER] Updated call: id=${updated.id}, status=${updated.status}"
                            )
                            _uiState.update {
                                it.copy(
                                    call = updated,
                                    message = "상대방이 통화를 수락했습니다."
                                )
                            }

                            // ⏱️ 타이머 시작
                            startTimer()

                            Log.d(TAG, "✓ [CALLER] UI state updated with CONNECTED call")
                        }
                        .onFailure { e ->
                            Log.e(
                                TAG,
                                "Failed to update call status on ACCEPT: ${e.message}",
                                e
                            )
                        }
                }
            }
            "REJECT" -> {
                Log.d(TAG, "❌ Remote user rejected the call")
                _uiState.update {
                    it.copy(
                        call = currentCall?.copy(status = "REJECTED"),
                        message = "상대방이 통화를 거절했습니다."
                    )
                }
                webRtcCallClient.endCall()
                voipSignalingService.disconnect()
            }
            "HANGUP" -> {
                Log.d(TAG, "📞 Remote user hung up: ${signal.reason}")

                // ⏱️ 타이머 정지
                stopTimer()

                val call = currentCall
                if (call != null && call.status != "ENDED") {
                    viewModelScope.launch {
                        callRepository.updateVoipCallStatus(call.id, "ENDED")
                            .onSuccess { updated ->
                                currentCall = updated
                                Log.d(TAG, "✓ Call status updated to ENDED")
                                _uiState.update {
                                    it.copy(
                                        call = updated,
                                        message = "상대방이 통화를 종료했습니다.",
                                        shouldFinish = true
                                    )
                                }
                            }
                            .onFailure { e ->
                                Log.e(
                                    TAG,
                                    "Failed to update call status on HANGUP: ${e.message}, but cleaning up anyway",
                                    e
                                )
                                // 409 에러 등으로 이미 종료된 경우에도 로컬 상태 업데이트
                                currentCall = call.copy(status = "ENDED")
                                _uiState.update {
                                    it.copy(
                                        call = call.copy(status = "ENDED"),
                                        message = "상대방이 통화를 종료했습니다.",
                                        shouldFinish = true
                                    )
                                }
                            }

                        // 성공/실패 관계없이 항상 리소스 정리
                        webRtcCallClient.endCall()
                        voipSignalingService.disconnect()
                        Log.d(TAG, "✓ WebRTC and signaling cleaned up on HANGUP")
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            message = "상대방이 통화를 종료했습니다.",
                            shouldFinish = true
                        )
                    }
                    webRtcCallClient.endCall()
                    voipSignalingService.disconnect()
                }
            }
        }
    }

    /**
     * 📌 수신자가 OFFER/ICE를 처리하기 전에
     * TURN + WebRTC + ICE 리스너까지 준비해두는 함수
     */
    private suspend fun ensureWebRtcInitializedForCallee(callId: Long, senderId: Long) {
        // 이미 currentCall이 있고 id도 맞으면 그대로 사용
        if (isWebRtcInitialized) {
            Log.d(TAG, "WebRTC already initialized, skipping")
            return
        }
        if (currentCall == null || currentCall?.id != callId) {
            callRepository.getVoipCall(callId)
                .onSuccess { call ->
                    currentCall = call
                    _uiState.update { it.copy(call = call) }
                    Log.d(TAG, "[CALLEE] ensureWebRtcInitializedForCallee: call loaded id=${call.id}")
                }
                .onFailure { e ->
                    Log.e(
                        TAG,
                        "[CALLEE] getVoipCall failed in ensureWebRtcInitializedForCallee: ${e.message}",
                        e
                    )
                }
        }

        val call = currentCall
        if (call == null) {
            Log.e(TAG, "[CALLEE] currentCall is still null in ensureWebRtcInitializedForCallee")
            return
        }

        val turn = callRepository.getTurnCredentials().getOrThrow()
        val iceServers = turn.toIceServers()
        webRtcCallClient.init(iceServers)
        Log.d(TAG, "[CALLEE] WebRTC init done in ensureWebRtcInitializedForCallee()")

        // PeerConnection 상태 모니터링
        setupPeerConnectionStateMonitoring()

        // ICE candidate 리스너 설정 (수신자 입장에서 상대는 발신자)
        val myUserId = currentUserId
        val callerId = call.callerId

        if (myUserId != null && callerId != null) {
            setupIceCandidateListener(
                call.id,
                call.sessionId,
                myUserId,
                callerId
            )
        } else {
            Log.w(
                TAG,
                "[CALLEE] ensureWebRtcInitializedForCallee: myUserId=$myUserId, callerId=$callerId → ICE 전송 설정 못함"
            )
        }
        isWebRtcInitialized = true
    }

    private fun handleOffer(sdp: String, callId: Long, senderId: Long) {
        Log.d(TAG, "Handling OFFER from $senderId for callId=$callId")

        viewModelScope.launch {
            // 0. 수신자 WebRTC 초기화 먼저
            ensureWebRtcInitializedForCallee(callId, senderId)

            // Remote Description 설정
            webRtcCallClient.setRemoteDescription(
                type = SessionDescription.Type.OFFER,
                sdp = sdp
            )
            isRemoteDescriptionSet = true  // 플래그 설정

            // 버퍼링된 ICE candidate 처리
            processPendingIceCandidates()

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
    }

    private fun handleAnswer(sdp: String) {
        Log.d(TAG, "Handling ANSWER")

        // 1. Remote Answer SDP 설정
        webRtcCallClient.setRemoteDescription(
            type = SessionDescription.Type.ANSWER,
            sdp = sdp
        )
        isRemoteDescriptionSet = true  // 플래그 설정
        Log.d(TAG, "Remote Answer SDP set")

        processPendingIceCandidates()

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
        if (!isRemoteDescriptionSet) {
            Log.d(TAG, "Remote description not set yet, buffering ICE candidate")
            pendingIceCandidates.add(
                IceCandidateInfo(candidate, sdpMid, sdpMLineIndex)
            )
            return
        }

        Log.d(TAG, "Adding remote ICE candidate")
        webRtcCallClient.addRemoteIceCandidate(sdpMid, sdpMLineIndex, candidate)
    }

    private fun processPendingIceCandidates() {
        Log.d(TAG, "Processing ${pendingIceCandidates.size} buffered ICE candidates")
        pendingIceCandidates.forEach { ice ->
            webRtcCallClient.addRemoteIceCandidate(
                ice.sdpMid,
                ice.sdpMLineIndex,
                ice.candidate
            )
        }
        pendingIceCandidates.clear()
    }

    // =========================================================
    // 🔧 수신자 WebRTC 초기화 (accept 이후 호출)
    // =========================================================

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

    // =========================================================
    // ⏱️ 타이머 관리
    // =========================================================

    private fun startTimer() {
        // 이미 타이머가 실행 중이면 중복 실행 방지
        if (callTimerJob?.isActive == true) {
            Log.d(TAG, "Timer already running, skip startTimer()")
            return
        }

        Log.d(TAG, "✓ Timer started")
        callTimerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                delay(1000)
                seconds++
                _uiState.update { it.copy(callDurationSeconds = seconds) }
            }
        }
    }

    private fun stopTimer() {
        callTimerJob?.cancel()
        callTimerJob = null
        _uiState.update { it.copy(callDurationSeconds = 0) }
        Log.d(TAG, "✓ Timer stopped and reset")
    }

    // =========================================================

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "VoipCallViewModel onCleared")

        val currentStatus = currentCall?.status
        Log.d(TAG, "onCleared called with call status: $currentStatus")

        // 통화 중(CONNECTED)이거나 연결 중(RINGING)일 때는 리소스를 정리하지 않음
        // Activity 재생성 등으로 인한 의도치 않은 종료 방지
        if (currentStatus == "CONNECTED" || currentStatus == "RINGING") {
            Log.w(TAG, "⚠️ Call is active ($currentStatus), skipping resource cleanup to prevent premature termination")
            // 타이머는 정리하지 않고, WebSocket/WebRTC도 유지
            return
        }

        // 통화가 종료되었거나 시작되지 않은 경우에만 리소스 정리
        Log.d(TAG, "✓ Cleaning up resources (status: $currentStatus)")
        stopTimer()
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
