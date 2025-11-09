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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.ongil.common.location.LocationPoint
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.core.webrtc.WebRtcCallClient
import kr.co.ongil.data.model.call.CallStartLocationRequest
import kr.co.ongil.data.model.call.TurnCredentialsDto
import kr.co.ongil.data.model.call.VoipCallDto
import kr.co.ongil.domain.repository.CallRepository
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import java.time.Instant

@HiltViewModel
class VoipCallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val locationStreamBus: LocationStreamBus,
    private val application: Application,
    private val webRtcCallClient: WebRtcCallClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoipCallUiState())
    val uiState: StateFlow<VoipCallUiState> = _uiState.asStateFlow()

    private var currentCall: VoipCallDto? = null

    init {
        // 위치 스트림 구독 (디버깅 / UI 표시용)
        viewModelScope.launch {
            locationStreamBus.updates.collect { point ->
                _uiState.update {
                    it.copy(
                        currentLocation = "위도: ${point.latitude}, 경도: ${point.longitude}, 정확도: ${point.accuracyMeters}m"
                    )
                }
            }
        }
    }

    /** 📞 발신 (우리 앱 VOIP) */
    fun startVoipCall(
        receiverId: Long,
        userType: String,
        callType: String = "NORMAL"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, error = null) }

            callRepository.createVoipCall(receiverId, callType)
                .onSuccess { call ->
                    currentCall = call

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            call = call,
                            message = "통화 요청 완료"
                        )
                    }

                    // 환자가 발신자면 통화 시작 시 위치 1회 전송
                    if (userType == "PATIENT") {
                        sendStartLocationOnce(call.id)
                    }

                    // WebRTC: 발신자 초기화 (시그널링은 TODO)
                    setupWebRtcAsCaller()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "통화 요청 실패"
                        )
                    }
                }
        }
    }

    /** 📲 수신: callId 기반 통화 정보 조회 */
    fun loadIncomingCall(callId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null, error = null) }

            callRepository.getVoipCall(callId)
                .onSuccess { call ->
                    currentCall = call
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            call = call
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "통화 정보 조회 실패"
                        )
                    }
                }
        }
    }

    /** ✅ 수신자가 통화 수락 */
    fun acceptCall(userType: String) {
        val callId = currentCall?.id ?: return
        viewModelScope.launch {
            callRepository.updateVoipCallStatus(callId, "CONNECTED")
                .onSuccess { updated ->
                    currentCall = updated
                    _uiState.update {
                        it.copy(
                            call = updated,
                            message = "통화 연결됨"
                        )
                    }

                    // 환자가 수신자면 위치 1회 전송
                    if (userType == "PATIENT") {
                        sendStartLocationOnce(callId)
                    }

                    // WebRTC: 수신자 초기화 (시그널링은 TODO)
                    setupWebRtcAsCallee()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "통화 수락 실패"
                        )
                    }
                }
        }
    }

    /** ❌ 통화 종료 */
    fun endCall() {
        val callId = currentCall?.id ?: return
        viewModelScope.launch {
            callRepository.updateVoipCallStatus(callId, "ENDED")
                .onSuccess {
                    webRtcCallClient.endCall()
                    _uiState.update {
                        it.copy(
                            message = "통화 종료됨"
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "통화 종료 실패"
                        )
                    }
                }
        }
    }

    // ========================
    // WebRTC 연동 (Offer/Answer 자리만 정의)
    // ========================

    /** 발신자: TURN 조회 + WebRTC 초기화 + Offer 생성 */
    private suspend fun setupWebRtcAsCaller() {
        val call = currentCall ?: return

        callRepository.getTurnCredentials()
            .onSuccess { turn ->
                val iceServers = turn.toIceServers()
                webRtcCallClient.init(iceServers)

                // Offer 생성 → (여기서 백엔드/시그널링 서버로 전송해야 함)
                webRtcCallClient.createOffer { sdp ->
                    Log.d("VoipCallViewModel", "Offer SDP created: ${sdp.type}")
                    // TODO: signalingClient.sendOffer(call.sessionId, sdp.description)
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(error = "TURN 정보 조회 실패: ${e.message}")
                }
            }
    }

    /** 수신자: TURN 조회 + WebRTC 초기화 + Answer 생성 */
    private suspend fun setupWebRtcAsCallee() {
        val call = currentCall ?: return

        callRepository.getTurnCredentials()
            .onSuccess { turn ->
                val iceServers = turn.toIceServers()
                webRtcCallClient.init(iceServers)

                // ⚠ 실제 구현 시:
                // 1) 발신자의 OFFER SDP를 시그널링으로 받아서
                // 2) webRtcCallClient.setRemoteDescription(SessionDescription.Type.OFFER, offerSdp)
                // 3) 그 다음 createAnswer 호출해야 함

                webRtcCallClient.createAnswer { sdp ->
                    Log.d("VoipCallViewModel", "Answer SDP created: ${sdp.type}")
                    // TODO: signalingClient.sendAnswer(call.sessionId, sdp.description)
                }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(error = "TURN 정보 조회 실패: ${e.message}")
                }
            }
    }

    // ========================
    // 위치 관련
    // ========================

    /** 현재 위치 테스트용 (원하면 호출) */
    fun fetchCurrentLocation() {
        viewModelScope.launch {
            if (!hasLocationPermission()) {
                _uiState.update {
                    it.copy(error = "⚠️ 위치 권한이 필요합니다. 앱 설정에서 위치 권한을 허용해주세요.")
                }
                Log.w("VoipCallViewModel", "위치 권한 없음")
                return@launch
            }

            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(application)
                val lastLocation = fusedClient.lastLocation.await()

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
            } catch (e: SecurityException) {
                _uiState.update {
                    it.copy(error = "⚠️ 위치 권한이 거부되었습니다: ${e.message}")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "위치 가져오기 실패: ${e.message}")
                }
            }
        }
    }

    /** 위치 권한 체크 */
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

    /** 통화 시작/수락 시 위치 1회 전송 */
    private fun sendStartLocationOnce(callId: Long) {
        viewModelScope.launch {
            if (!hasLocationPermission()) {
                _uiState.update {
                    it.copy(error = "⚠️ 위치 권한이 필요합니다. 앱 설정에서 위치 권한을 허용해주세요.")
                }
                Log.w("VoipCallViewModel", "위치 권한 없음 - 전송 실패")
                return@launch
            }

            // 1) LocationStreamBus 최신 값 사용
            var point = locationStreamBus.lastValue

            // 2) 없으면 FusedLocation에서 fallback
            if (point == null) {
                try {
                    val fusedClient = LocationServices.getFusedLocationProviderClient(application)
                    val lastLocation = fusedClient.lastLocation.await()
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
                    Log.e("VoipCallViewModel", "FusedLocation fallback 실패", e)
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

            Log.d("VoipCallViewModel", "위치 전송: ${point.latitude}, ${point.longitude}")

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
}

/** TURN DTO → ICE 서버 리스트 변환 (이 파일 안에서만 사용) */
private fun TurnCredentialsDto.toIceServers(): List<PeerConnection.IceServer> =
    uris.map {
        PeerConnection.IceServer.builder(it)
            .setUsername(username)
            .setPassword(credential)
            .createIceServer()
    }