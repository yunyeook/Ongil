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
    private var localAudioTrack: AudioTrack? = null
    private var remoteAudioTrack: AudioTrack? = null  // ✅ 원격 트랙 관리

    private var audioDeviceModule: JavaAudioDeviceModule? = null

    private var onLocalIceCandidate: ((IceCandidate) -> Unit)? = null
    private var onPeerConnectionStateChange: ((PeerConnection.PeerConnectionState) -> Unit)? = null

    private val pendingIceCandidates = mutableListOf<IceCandidate>()
    private var remoteDescriptionSet = false

    // ✅ 초기화 상태 플래그
    private var isInitialized = false

    fun setOnLocalIceCandidateListener(listener: (IceCandidate) -> Unit) {
        onLocalIceCandidate = listener
    }

    fun setOnPeerConnectionStateChangeListener(listener: (PeerConnection.PeerConnectionState) -> Unit) {
        onPeerConnectionStateChange = listener
    }

    fun init(iceServers: List<PeerConnection.IceServer>) {
        // ✅ 중복 초기화 방지
        if (isInitialized) {
            Log.w(TAG, "⚠️ Already initialized, cleaning up first...")
            cleanup()
        }

        Log.d(TAG, "🎬 Initializing WebRTC with ${iceServers.size} ICE servers")

        // ICE 관련 변수 초기화
        pendingIceCandidates.clear()
        remoteDescriptionSet = false

        try {
            // 1) Factory 생성
            factory = buildPeerConnectionFactory()

            // 2) 오디오 Source/Track 생성
            val audioConstraints = MediaConstraints()
            audioSource = factory?.createAudioSource(audioConstraints)
            localAudioTrack = factory?.createAudioTrack(AUDIO_TRACK_ID, audioSource).apply {
                this?.setEnabled(true)
                Log.d(TAG, "✅ Local audio track created and enabled")
            }

            // 3) PeerConnection 생성
            val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                // ✅ 연결 안정성 향상을 위한 설정
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            }

            peer = factory?.createPeerConnection(
                rtcConfig,
                object : PeerConnection.Observer {
                    override fun onIceCandidate(candidate: IceCandidate) {
                        Log.d(TAG, "🧊 Local ICE candidate: ${candidate.sdp.take(50)}...")

                        if (remoteDescriptionSet) {
                            onLocalIceCandidate?.invoke(candidate)
                            Log.d(TAG, "📤 ICE candidate sent immediately")
                        } else {
                            pendingIceCandidates.add(candidate)
                            Log.d(TAG, "📦 ICE candidate queued (${pendingIceCandidates.size} total)")
                        }
                    }

                    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                        Log.d(TAG, "🔗 Peer connection state: $newState")
                        onPeerConnectionStateChange?.invoke(newState)
                    }

                    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                        Log.d(TAG, "🧊 ICE connection state: $state")
                    }

                    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
                        Log.d(TAG, "🔍 ICE gathering: $state")
                    }

                    override fun onSignalingChange(state: PeerConnection.SignalingState) {
                        Log.d(TAG, "📡 Signaling state: $state")
                    }

                    override fun onAddTrack(
                        receiver: org.webrtc.RtpReceiver,
                        mediaStreams: Array<out org.webrtc.MediaStream>
                    ) {
                        Log.d(TAG, "📥 onAddTrack: receiver=$receiver, streams=${mediaStreams.size}")

                        val track = receiver.track()
                        if (track?.kind() == "audio") {
                            remoteAudioTrack = track as? AudioTrack
                            remoteAudioTrack?.setEnabled(true)
                            remoteAudioTrack?.setVolume(10.0)  // ✅ 볼륨 최대
                            Log.d(TAG, "✅ Remote audio track enabled with max volume")
                        }
                    }

                    override fun onIceConnectionReceivingChange(p0: Boolean) {
                        Log.d(TAG, "📊 ICE receiving: $p0")
                    }

                    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
                    override fun onDataChannel(p0: org.webrtc.DataChannel) {}
                    override fun onRenegotiationNeeded() {
                        Log.d(TAG, "🔄 Renegotiation needed")
                    }
                    override fun onAddStream(p0: org.webrtc.MediaStream) {}
                    override fun onRemoveStream(p0: org.webrtc.MediaStream) {}
                }
            )

            // 4) Local Audio Track 추가
            localAudioTrack?.let { track ->
                val sender = peer?.addTrack(track, listOf(LOCAL_STREAM_ID))
                Log.d(TAG, "✅ Local audio track added to PeerConnection")
            } ?: run {
                Log.e(TAG, "❌ Local audio track is null!")
            }

            isInitialized = true
            Log.d(TAG, "✅ WebRTC initialization complete")

        } catch (e: Exception) {
            Log.e(TAG, "❌ WebRTC initialization failed", e)
            cleanup()
            throw e
        }
    }

    fun createOffer(onSdpReady: (SessionDescription) -> Unit) {
        val pc = peer ?: run {
            Log.e(TAG, "❌ PeerConnection is null in createOffer")
            return
        }

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        Log.d(TAG, "📞 Creating offer...")
        pc.createOffer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "✅ Offer created (${sdp.description.length} chars)")

                pc.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        Log.d(TAG, "✅ Local description set (offer)")
                        onSdpReady(sdp)
                    }

                    override fun onSetFailure(reason: String) {
                        Log.e(TAG, "❌ Failed to set local description: $reason")
                    }
                }, sdp)
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "❌ Failed to create offer: $error")
            }
        }, constraints)
    }

    fun createAnswer(onSdpReady: (SessionDescription) -> Unit) {
        val pc = peer ?: run {
            Log.e(TAG, "❌ PeerConnection is null in createAnswer")
            return
        }

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        Log.d(TAG, "📞 Creating answer...")
        pc.createAnswer(object : SimpleSdpObserver() {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "✅ Answer created (${sdp.description.length} chars)")

                pc.setLocalDescription(object : SimpleSdpObserver() {
                    override fun onSetSuccess() {
                        Log.d(TAG, "✅ Local description set (answer)")
                        onSdpReady(sdp)
                    }

                    override fun onSetFailure(reason: String) {
                        Log.e(TAG, "❌ Failed to set local description: $reason")
                    }
                }, sdp)
            }

            override fun onCreateFailure(error: String) {
                Log.e(TAG, "❌ Failed to create answer: $error")
            }
        }, constraints)
    }

    fun setRemoteDescription(type: SessionDescription.Type, sdp: String) {
        val pc = peer ?: run {
            Log.e(TAG, "❌ PeerConnection is null in setRemoteDescription")
            return
        }

        Log.d(TAG, "📥 Setting remote description: $type (${sdp.length} chars)")
        val desc = SessionDescription(type, sdp)

        pc.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                Log.d(TAG, "✅ Remote description set: $type")

                remoteDescriptionSet = true

                // ✅ 대기 중인 ICE candidates 처리
                if (pendingIceCandidates.isNotEmpty()) {
                    Log.d(TAG, "📤 Sending ${pendingIceCandidates.size} queued ICE candidates")

                    pendingIceCandidates.forEach { candidate ->
                        onLocalIceCandidate?.invoke(candidate)
                    }

                    pendingIceCandidates.clear()
                    Log.d(TAG, "✅ All queued ICE candidates sent")
                }
            }

            override fun onSetFailure(reason: String) {
                Log.e(TAG, "❌ Failed to set remote description: $reason")
            }
        }, desc)
    }

    fun addRemoteIceCandidate(sdpMid: String?, sdpMLineIndex: Int, candidate: String) {
        val pc = peer ?: run {
            Log.e(TAG, "❌ PeerConnection is null in addRemoteIceCandidate")
            return
        }

        Log.d(TAG, "🧊 Adding remote ICE: mid=$sdpMid, line=$sdpMLineIndex")

        try {
            val ice = IceCandidate(sdpMid, sdpMLineIndex, candidate)
            val success = pc.addIceCandidate(ice)
            Log.d(TAG, if (success) "✅ ICE candidate added" else "❌ Failed to add ICE candidate")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception adding ICE candidate", e)
        }
    }

    fun endCall() {
        Log.d(TAG, "🛑 Ending call...")
        cleanup()
    }

    private fun cleanup() {
        try {
            Log.d(TAG, "🧹 Cleaning up WebRTC resources...")

            // ✅ 순서가 매우 중요합니다!

            // 1. Remote audio track (PeerConnection이 관리하므로 dispose 하지 않음)
            try {
                remoteAudioTrack?.setEnabled(false)
                remoteAudioTrack = null
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error disabling remote audio track: ${e.message}")
            }

            // 2. Local audio track
            try {
                localAudioTrack?.setEnabled(false)
                localAudioTrack?.dispose()
                localAudioTrack = null
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error disposing local audio track: ${e.message}")
            }

            // 3. Audio source
            try {
                audioSource?.dispose()
                audioSource = null
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error disposing audio source: ${e.message}")
            }

            // 4. PeerConnection (close 먼저, dispose 나중에)
            try {
                peer?.close()
                peer?.dispose()
                peer = null
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error disposing peer connection: ${e.message}")
            }

            // 5. AudioDeviceModule
            try {
                audioDeviceModule?.release()
                audioDeviceModule = null
                Log.d(TAG, "✅ AudioDeviceModule released")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error releasing AudioDeviceModule: ${e.message}")
            }

            // 6. Factory
            try {
                factory?.dispose()
                factory = null
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Error disposing factory: ${e.message}")
            }

            Log.d(TAG, "✅ WebRTC cleanup complete")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during cleanup", e)
        } finally {
            isInitialized = false
            pendingIceCandidates.clear()
            remoteDescriptionSet = false
        }
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        val initOptions = PeerConnectionFactory.InitializationOptions
            .builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()

        PeerConnectionFactory.initialize(initOptions)

        // ✅ AudioDeviceModule 생성 (한 번만)
        audioDeviceModule = JavaAudioDeviceModule.builder(context)
            .setUseHardwareAcousticEchoCanceler(true)
            .setUseHardwareNoiseSuppressor(true)
            .setAudioRecordErrorCallback(object : JavaAudioDeviceModule.AudioRecordErrorCallback {
                override fun onWebRtcAudioRecordError(errorMessage: String?) {
                    Log.e(TAG, "🎤 Audio record error: $errorMessage")
                }

                override fun onWebRtcAudioRecordInitError(errorMessage: String?) {
                    Log.e(TAG, "🎤 Audio record init error: $errorMessage")
                }

                override fun onWebRtcAudioRecordStartError(
                    errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
                    errorMessage: String?
                ) {
                    Log.e(TAG, "🎤 Audio record start error: $errorCode - $errorMessage")
                }
            })
            .setAudioTrackErrorCallback(object : JavaAudioDeviceModule.AudioTrackErrorCallback {
                override fun onWebRtcAudioTrackError(errorMessage: String?) {
                    Log.e(TAG, "🔊 Audio track error: $errorMessage")
                }

                override fun onWebRtcAudioTrackInitError(errorMessage: String?) {
                    Log.e(TAG, "🔊 Audio track init error: $errorMessage")
                }

                override fun onWebRtcAudioTrackStartError(
                    errorCode: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
                    errorMessage: String?
                ) {
                    Log.e(TAG, "🔊 Audio track start error: $errorCode - $errorMessage")
                }
            })
            .createAudioDeviceModule()

        Log.d(TAG, "✅ AudioDeviceModule created")

        return PeerConnectionFactory
            .builder()
            .setAudioDeviceModule(audioDeviceModule)
            .createPeerConnectionFactory()
    }

    open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(reason: String) {
            Log.e(TAG, "❌ SDP creation failed: $reason")
        }
        override fun onSetFailure(reason: String) {
            Log.e(TAG, "❌ SDP set failed: $reason")
        }
    }

    companion object {
        private const val TAG = "WebRtcCallClient"
        private const val LOCAL_STREAM_ID = "LOCAL_AUDIO_STREAM"
        private const val AUDIO_TRACK_ID = "AUDIO_TRACK"
    }
}