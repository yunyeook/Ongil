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

/**
 * 우리 앱 VOIP용 WebRTC 클라이언트 (음성 전용)
 *
 * - TURN/STUN 정보(ICE 서버 리스트)를 받아 PeerConnection 초기화
 * - Offer / Answer 생성
 * - Remote SDP / ICE Candidate 설정
 * - 실제 시그널링 전송(WebSocket 등)은 외부(뷰모델/별도 클래스)에서 처리
 *
 * 사용 흐름 (ViewModel 기준):
 *  1) getTurnCredentials()로 TURN 정보 받음
 *  2) TURN → IceServer 리스트 만든 후 webRtcCallClient.init(iceServers)
 *  3) 발신자: createOffer { sdp -> 서버로 전송 }
 *  4) 수신자: setRemoteDescription(offerSdp) 후 createAnswer { sdp -> 서버로 전송 }
 *  5) 서로 addRemoteIceCandidate(...) 호출
 *  6) 통화 종료 시 endCall()
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
            Log.d(TAG, "WebRTC already initialized")
            return
        }

        // 1) 전역 초기화
        val initOptions = PeerConnectionFactory.InitializationOptions
            .builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initOptions)

        // 2) Factory 생성
        factory = PeerConnectionFactory.builder()
            .createPeerConnectionFactory()

        // 3) 오디오 Source/Track 생성
        val audioConstraints = MediaConstraints()
        audioSource = factory?.createAudioSource(audioConstraints)
        audioTrack = factory?.createAudioTrack(AUDIO_TRACK_ID, audioSource)

        // 4) PeerConnection 생성 (Unified Plan SDP 사용)
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
        peer = factory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d(TAG, "Local ICE candidate: $candidate")
                    onLocalIceCandidate?.invoke(candidate)
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
                    p0: org.webrtc.RtpReceiver,
                    p1: Array<out org.webrtc.MediaStream>
                ) {}
            }
        )

        // 5) Unified Plan에서는 addTrack() 사용 (addStream 대신)
        audioTrack?.let { track ->
            peer?.addTrack(track, listOf(LOCAL_STREAM_ID))
            Log.d(TAG, "Audio track added to PeerConnection")
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
            Log.e(TAG, "PeerConnection is null in createOffer")
            return
        }

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        pc.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "Offer created")
                pc.setLocalDescription(this, sdp)
                onSdpReady(sdp)
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
            Log.e(TAG, "PeerConnection is null in createAnswer")
            return
        }

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        pc.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "Answer created")
                pc.setLocalDescription(this, sdp)
                onSdpReady(sdp)
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "Answer create failed: $error")
            }
        }, constraints)
    }

    /**
     * 원격 SDP 설정 (Offer 또는 Answer)
     * - type: "offer" 또는 "answer"
     */
    fun setRemoteDescription(type: SessionDescription.Type, sdp: String) {
        val pc = peer ?: run {
            Log.e(TAG, "PeerConnection is null in setRemoteDescription")
            return
        }

        val desc = SessionDescription(type, sdp)
        pc.setRemoteDescription(object : SimpleSdpObserver() {}, desc)
    }

    /**
     * 원격 ICE Candidate 추가
     * - 시그널링 서버를 통해 받은 candidate 정보를 그대로 넣어주면 됨
     */
    fun addRemoteIceCandidate(sdpMid: String?, sdpMLineIndex: Int, candidate: String) {
        val pc = peer ?: run {
            Log.e(TAG, "PeerConnection is null in addRemoteIceCandidate")
            return
        }
        pc.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, candidate))
    }

    /**
     * 통화 종료 / 리소스 해제
     */
    fun endCall() {
        try {
            Log.d(TAG, "endCall()")
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
}