package kr.co.ongil.core.webrtc

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule

/**
 * 우리 앱 VOIP용 WebRTC 클라이언트 (음성 전용)
 */
class WebRtcCallClient @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var factory: PeerConnectionFactory? = null
    private var peer: PeerConnection? = null
    private var audioSource: AudioSource? = null
    private var audioTrack: AudioTrack? = null

    // 로컬에서 생성된 ICE Candidate를 바깥(시그널링)으로 전달하는 콜백
    private var onLocalIceCandidate: ((IceCandidate) -> Unit)? = null

    // PeerConnection 상태 변경 콜백
    private var onPeerConnectionStateChange: ((PeerConnection.PeerConnectionState) -> Unit)? = null

    // ICE Candidate 대기열 (Remote Description 설정 전까지 보관)
    private val pendingIceCandidates = mutableListOf<IceCandidate>()

    // Remote Description 설정 여부 플래그
    private var remoteDescriptionSet = false

    fun setOnLocalIceCandidateListener(listener: (IceCandidate) -> Unit) {
        onLocalIceCandidate = listener
    }

    fun setOnPeerConnectionStateChangeListener(listener: (PeerConnection.PeerConnectionState) -> Unit) {
        onPeerConnectionStateChange = listener
    }




    /**
     * ICE 서버 정보로 WebRTC 초기화
     * - 반드시 Offer/Answer 생성 전에 한 번 호출
     */
    fun init(iceServers: List<PeerConnection.IceServer>) {
        if (factory != null && peer != null) {
            Log.d(TAG, "init() called but WebRTC already initialized")
            return
        }

        // ✅ ICE 관련 변수 초기화
        pendingIceCandidates.clear()
        remoteDescriptionSet = false

        Log.d(
            TAG,
            "Initializing WebRTC with iceServers=${iceServers.joinToString { it.uri }}"
        )

        // 1) Factory 생성 (AudioDeviceModule 포함)
        factory = buildPeerConnectionFactory()

        // 2) 오디오 Source/Track 생성
        val audioConstraints = MediaConstraints()
        audioSource = factory?.createAudioSource(audioConstraints)
        audioTrack = factory?.createAudioTrack(AUDIO_TRACK_ID, audioSource).apply {
            this?.setEnabled(true) // 🔹 명시적으로 활성화
        }

        // 3) PeerConnection 생성 (Unified Plan SDP 사용)
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        peer = factory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d(TAG, "🧊 Local ICE candidate generated: type=${candidate.sdp}")

                    // ✅ Remote Description 설정 여부 확인
                    if (remoteDescriptionSet) {
                        // 이미 설정됨 → 즉시 전송
                        onLocalIceCandidate?.invoke(candidate)
                        Log.d(TAG, "📤 ICE candidate 즉시 전송")
                    } else {
                        // 아직 미설정 → 대기열에 저장
                        pendingIceCandidates.add(candidate)
                        Log.d(TAG, "📦 ICE candidate 대기열 추가 (대기: ${pendingIceCandidates.size}개)")
                    }
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                    Log.d(TAG, "Peer connection state: $newState")
                    onPeerConnectionStateChange?.invoke(newState)
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    Log.d(TAG, "ICE connection state: $state")
                }

                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
                    Log.d(TAG, "ICE gathering: $state")
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState) {
                    Log.d(TAG, "Signaling state: $state")
                }

                override fun onIceConnectionReceivingChange(p0: Boolean) {}
                override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
                override fun onDataChannel(p0: org.webrtc.DataChannel) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddStream(p0: org.webrtc.MediaStream) {}
                override fun onRemoveStream(p0: org.webrtc.MediaStream) {}

                override fun onAddTrack(
                    receiver: org.webrtc.RtpReceiver,
                    mediaStreams: Array<out org.webrtc.MediaStream>
                ) {
                    Log.d(TAG, "onAddTrack() called. receiver=$receiver, streams=${mediaStreams.size}")
                    // 오디오 전용일 경우, 별도 renderer 없이도 기본 AudioDeviceModule이 재생 처리
                }
            }
        )

        // 4) Unified Plan에서는 addTrack() 사용
        audioTrack?.let { track ->
            val sender = peer?.addTrack(track, listOf(LOCAL_STREAM_ID))
            Log.d(TAG, "Audio track added to PeerConnection. sender=$sender")
        } ?: run {
            Log.e(TAG, "Audio track is null, WebRTC init may be wrong")
        }

        Log.d(TAG, "WebRTC initialized with ${iceServers.size} ICE servers")
    }

    /**
     * 발신자: Offer 생성
     * - init() 이후 호출
     * - 콜백으로 넘어오는 SDP를 시그널링 서버로 보내야 함
     */

    fun createOffer(onSdpReady: (SessionDescription) -> Unit) {
        val pc = peer ?: run {
            Log.e(TAG, "PeerConnection is null in createOffer. Did you call init()?")
            return
        }

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        }

        Log.d(TAG, "createOffer() called")
        pc.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "Offer created: type=${sdp.type}, length=${sdp.description.length}")
                pc.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        Log.d(TAG, "Local SDP set success (offer)")
                        onSdpReady(sdp)
                    }

                    override fun onSetFailure(reason: String) {
                        Log.e(TAG, "Local SDP set failure (offer): $reason")
                    }
                }, sdp)
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Offer create failed: $error")
            }
        }, constraints)
    }

    /**
     * 수신자: Answer 생성
     * - remote Offer를 setRemoteDescription 한 뒤 호출
     */

    fun createAnswer(onSdpReady: (SessionDescription) -> Unit) {
        val pc = peer ?: run {
            Log.e(TAG, "PeerConnection is null in createAnswer. Did you call init() and setRemoteDescription(offer)?")
            return
        }

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"))
        }

        Log.d(TAG, "createAnswer() called")
        pc.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "Answer created: type=${sdp.type}, length=${sdp.description.length}")
                pc.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        Log.d(TAG, "Local SDP set success (answer)")
                        onSdpReady(sdp)
                    }

                    override fun onSetFailure(reason: String) {
                        Log.e(TAG, "Local SDP set failure (answer): $reason")
                    }
                }, sdp)
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Answer create failed: $error")
            }
        }, constraints)
    }

    /**
     * 원격 SDP 설정 (Offer 또는 Answer)
     */

    fun setRemoteDescription(type: SessionDescription.Type, sdp: String) {
        val pc = peer ?: run {
            Log.e(TAG, "PeerConnection is null in setRemoteDescription. Did you call init()?")
            return
        }

        Log.d(TAG, "setRemoteDescription() type=$type, length=${sdp.length}")
        val desc = SessionDescription(type, sdp)
        pc.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                Log.d(TAG, "✅ Remote SDP set success: $type")

                // ✅ 플래그 설정
                remoteDescriptionSet = true

                // ✅ 대기 중이던 ICE candidates 일괄 전송
                if (pendingIceCandidates.isNotEmpty()) {
                    Log.d(TAG, "📤 대기 중이던 ICE candidates 전송 (${pendingIceCandidates.size}개)")

                    for (candidate in pendingIceCandidates) {
                        onLocalIceCandidate?.invoke(candidate)
                    }

                    Log.d(TAG, "✅ 대기 중이던 ICE candidates 전송 완료")
                    pendingIceCandidates.clear()  // 대기열 비우기
                }
            }

            override fun onSetFailure(reason: String) {
                Log.e(TAG, "Remote SDP set failure ($type): $reason")
            }
        }, desc)
    }

    /**
     * 원격 ICE Candidate 추가
     */
    fun addRemoteIceCandidate(sdpMid: String?, sdpMLineIndex: Int, candidate: String) {
        val pc = peer ?: run {
            Log.e(TAG, "PeerConnection is null in addRemoteIceCandidate. Did you call init()?")
            return
        }
        Log.d(TAG, "addRemoteIceCandidate() mid=$sdpMid, line=$sdpMLineIndex, candidateLength=${candidate.length}")
        val ice = IceCandidate(sdpMid, sdpMLineIndex, candidate)
        val success = pc.addIceCandidate(ice)
        Log.d(TAG, "addIceCandidate success=$success")
    }

    /**
     * 통화 종료 / 리소스 해제
     */
    fun endCall() {
        try {
            Log.d(TAG, "endCall() called. Releasing WebRTC resources.")
            peer?.close()
            audioTrack?.dispose()
            audioSource?.dispose()
            factory?.dispose()
        } catch (e: Exception) {
            Log.e(TAG, "Error while ending call", e)
        } finally {
            peer = null
            audioTrack = null
            audioSource = null
            factory = null

            // ✅ ICE 관련 변수 초기화
            pendingIceCandidates.clear()
            remoteDescriptionSet = false
        }
    }

    // ========================
    // 내부용 SDP Observer
    // ========================

    open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(reason: String) {
            Log.e(TAG, "SDP onCreateFailure: $reason")
        }
        override fun onSetFailure(reason: String) {
            Log.e(TAG, "SDP onSetFailure: $reason")
        }
    }

    companion object {
        private const val TAG = "WebRtcCallClient"
        private const val LOCAL_STREAM_ID = "LOCAL_AUDIO_STREAM"
        private const val AUDIO_TRACK_ID = "AUDIO_TRACK"
    }


    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        // WebRTC 초기화
        val initOptions = PeerConnectionFactory.InitializationOptions
            .builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initOptions)

        // AudioDeviceModule 설정 (마이크/스피커 연결)
        val adm = JavaAudioDeviceModule.builder(context)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .createAudioDeviceModule()

        return PeerConnectionFactory
            .builder()
            .setAudioDeviceModule(adm)
            .createPeerConnectionFactory()
    }
}
