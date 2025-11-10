# Android 통화 기능 구현 가이드

## 📋 개요

본 문서는 Ongil Android 앱에서 **시스템 전화**와 **VoIP 통화** 기능을 구현하는 방법을 안내합니다.

### 통화 유형

| 유형 | source | 설명 | 구현 방식 |
|------|--------|------|----------|
| **시스템 전화** | `SYSTEM_DIALER` | Android 기본 전화 앱 사용 | `Intent.ACTION_CALL` + CallLog 기록 |
| **VoIP 통화** | `APP` | 앱 내 실시간 음성 통화 | WebRTC + WebSocket + FCM |

---

## 🏗️ 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                         Android App                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐         ┌──────────────┐                     │
│  │ 시스템 전화  │         │  VoIP 통화   │                     │
│  │(SYSTEM_DIALER)│         │    (APP)     │                     │
│  └──────┬───────┘         └──────┬───────┘                     │
│         │                        │                              │
│         │ Intent.ACTION_CALL     │ WebRTC + WebSocket          │
│         ↓                        ↓                              │
│  ┌──────────────┐         ┌──────────────┐                     │
│  │ Android Dialer│         │ WebRTC Client│                     │
│  └──────┬───────┘         └──────┬───────┘                     │
│         │                        │                              │
│         │ CallLog 조회          │ SignalMessage                │
│         ↓                        ↓                              │
│  ┌──────────────────────────────────────┐                      │
│  │    POST /api/v1/calls/logs           │ ← 통화 종료 후      │
│  │    (백엔드로 통화 기록 전송)         │                      │
│  └──────────────────────────────────────┘                      │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                         Backend Server                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐      ┌──────────────────┐                │
│  │ POST /calls      │      │ PUT /calls/{id}  │                │
│  │ (VoIP 통화 생성) │      │ (상태 업데이트)  │                │
│  └────────┬─────────┘      └──────────────────┘                │
│           │                                                     │
│           ├── FCM 푸시 전송 (앱 깨우기)                        │
│           ├── WebSocket INCOMING 시그널                        │
│           └── 상태: CREATED → RINGING                          │
│                                                                 │
│  ┌──────────────────────────────────────────┐                  │
│  │ WebSocket /api/ws                        │                  │
│  │ (OFFER, ANSWER, ICE, HANGUP 시그널링)    │                  │
│  └──────────────────────────────────────────┘                  │
│                                                                 │
│  ┌──────────────────────────────────────────┐                  │
│  │ GET /calls/rtc/turn-credentials          │                  │
│  │ (TURN/STUN 서버 자격증명)                │                  │
│  └──────────────────────────────────────────┘                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📱 시스템 전화 구현 (SYSTEM_DIALER)

### 개요
Android 기본 전화 앱을 사용하여 전화를 걸고, 통화 종료 후 백엔드로 통화 기록을 전송합니다.

### 구현 단계

#### 1단계: 권한 설정

**AndroidManifest.xml**:
```xml
<!-- 전화 걸기 권한 -->
<uses-permission android:name="android.permission.CALL_PHONE" />

<!-- 통화 기록 조회 권한 -->
<uses-permission android:name="android.permission.READ_CALL_LOG" />
```

**런타임 권한 요청** (MainActivity.kt):
```kotlin
val callPermissions = arrayOf(
    Manifest.permission.CALL_PHONE,
    Manifest.permission.READ_CALL_LOG
)

ActivityCompat.requestPermissions(this, callPermissions, 100)
```

---

#### 2단계: 전화 걸기

**SystemCallHelper.kt** (신규 파일):
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/core/utils/SystemCallHelper.kt

package kr.co.ongil.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import kotlinx.coroutines.delay
import java.util.Date

object SystemCallHelper {

    /**
     * 시스템 전화 걸기
     */
    fun makeCall(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }
            context.startActivity(intent)

            Log.d("SystemCall", "전화 시작: $phoneNumber")
        } catch (e: SecurityException) {
            Log.e("SystemCall", "전화 권한 없음", e)
            throw e
        }
    }

    /**
     * 최근 통화 기록 조회 (통화 종료 후 CallLog에서 가져오기)
     */
    suspend fun getLastCallLog(context: Context, phoneNumber: String): CallLogInfo? {
        // 통화가 CallLog에 기록될 때까지 대기 (2초)
        delay(2000)

        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        val selection = "${CallLog.Calls.NUMBER} = ?"
        val selectionArgs = arrayOf(phoneNumber)
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                val type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
                val duration = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION))

                return CallLogInfo(
                    phoneNumber = number,
                    type = type,
                    date = Date(date),
                    duration = duration
                )
            }
        }

        return null
    }

    data class CallLogInfo(
        val phoneNumber: String,
        val type: Int,          // CallLog.Calls.OUTGOING_TYPE, INCOMING_TYPE 등
        val date: Date,
        val duration: Int       // 초 단위
    )
}
```

---

#### 3단계: 통화 종료 감지 (BroadcastReceiver)

**CallStateReceiver.kt** (신규 파일):
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/service/call/CallStateReceiver.kt

package kr.co.ongil.service.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kr.co.ongil.core.utils.SystemCallHelper
import kr.co.ongil.domain.repository.CallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallStateReceiver : BroadcastReceiver() {

    @Inject
    lateinit var callRepository: CallRepository

    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var callStartTime: Long = 0
    private var phoneNumber: String? = null

    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                // 전화 수신 중
                phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                Log.d("CallState", "전화 수신: $phoneNumber")
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // 통화 시작
                if (lastState != TelephonyManager.CALL_STATE_OFFHOOK) {
                    callStartTime = System.currentTimeMillis()
                    Log.d("CallState", "통화 시작")
                }
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                // 통화 종료
                if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {
                    Log.d("CallState", "통화 종료 - CallLog 전송 시작")

                    phoneNumber?.let { number ->
                        CoroutineScope(Dispatchers.IO).launch {
                            sendCallLogToBackend(context, number)
                        }
                    }
                }
            }
        }

        lastState = when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
            TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
            else -> TelephonyManager.CALL_STATE_IDLE
        }
    }

    private suspend fun sendCallLogToBackend(context: Context, phoneNumber: String) {
        try {
            // CallLog에서 최근 통화 기록 조회
            val callLog = SystemCallHelper.getLastCallLog(context, phoneNumber)

            if (callLog != null) {
                // 백엔드 API 호출
                callRepository.createCallLog(
                    receiverPhoneNumber = phoneNumber,
                    callType = "NORMAL",
                    source = "SYSTEM_DIALER",
                    duration = callLog.duration,
                    startedAt = callLog.date.toInstant().toString(),
                    endedAt = Date().toInstant().toString()
                )

                Log.d("CallState", "CallLog 전송 성공: $phoneNumber, ${callLog.duration}초")
            }
        } catch (e: Exception) {
            Log.e("CallState", "CallLog 전송 실패", e)
        }
    }
}
```

**AndroidManifest.xml**:
```xml
<receiver
    android:name=".service.call.CallStateReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.PHONE_STATE" />
    </intent-filter>
</receiver>

<!-- 전화 상태 감지 권한 -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

---

#### 4단계: API 추가 (CallApi.kt)

**기존 파일 수정**:
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/data/datasource/remote/api/CallApi.kt

interface CallApi {
    // 기존 GET 메서드들...

    /**
     * 시스템 전화 통화 기록 생성 (POST /api/v1/calls/logs)
     */
    @POST("/api/v1/calls/logs")
    suspend fun createCallLog(
        @Body request: CreateCallLogRequest
    ): Response<CallLogResponse>
}
```

**CreateCallLogRequest.kt** (신규 파일):
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/data/model/call/CreateCallLogRequest.kt

package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

@Serializable
data class CreateCallLogRequest(
    val receiverPhoneNumber: String,
    val callType: String,            // "NORMAL" | "EMERGENCY"
    val source: String = "SYSTEM_DIALER",
    val duration: Int,               // 초 단위
    val startedAt: String,           // ISO8601
    val endedAt: String              // ISO8601
)
```

---

#### 5단계: Repository 구현

**CallRepository.kt** (인터페이스 추가):
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/domain/repository/CallRepository.kt

interface CallRepository {
    // 기존 메서드들...

    suspend fun createCallLog(
        receiverPhoneNumber: String,
        callType: String,
        source: String,
        duration: Int,
        startedAt: String,
        endedAt: String
    ): Result<Unit>
}
```

**CallRepositoryImpl.kt** (구현체 추가):
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/data/repository/CallRepositoryImpl.kt

override suspend fun createCallLog(
    receiverPhoneNumber: String,
    callType: String,
    source: String,
    duration: Int,
    startedAt: String,
    endedAt: String
): Result<Unit> {
    return try {
        val request = CreateCallLogRequest(
            receiverPhoneNumber = receiverPhoneNumber,
            callType = callType,
            source = source,
            duration = duration,
            startedAt = startedAt,
            endedAt = endedAt
        )

        val response = callApi.createCallLog(request)

        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("CallLog 생성 실패: ${response.code()}"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

#### 6단계: UI에서 전화 걸기

**PatientDetailScreen.kt** (예시):
```kotlin
// 환자 상세 화면에서 전화 버튼 클릭 시

Button(
    onClick = {
        try {
            SystemCallHelper.makeCall(context, patient.phoneNumber)
        } catch (e: SecurityException) {
            // 권한 요청 다이얼로그 표시
            showPermissionDialog = true
        }
    }
) {
    Icon(Icons.Default.Call, contentDescription = "전화 걸기")
    Text("전화 걸기")
}
```

---

## 🎙️ VoIP 통화 구현 (APP)

### 개요
WebRTC 기반 P2P 음성 통화를 구현합니다. FCM 푸시로 앱을 깨우고, WebSocket으로 시그널링을 수행합니다.

### 기술 스택
- **WebRTC**: 실시간 음성 통신
- **WebSocket (STOMP)**: 시그널링 서버
- **FCM**: 백그라운드 앱 깨우기

---

### 구현 단계

#### 1단계: 의존성 추가

**build.gradle.kts** (app 모듈):
```kotlin
dependencies {
    // WebRTC
    implementation("io.getstream:stream-webrtc-android:1.1.3")

    // WebSocket (STOMP)
    implementation("com.github.NaikSoftware:StompProtocolAndroid:1.6.6")
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    // OkHttp (WebSocket)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

---

#### 2단계: WebSocket 클라이언트 구현

**StompWebSocketClient.kt** (신규 파일):
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/data/datasource/remote/websocket/StompWebSocketClient.kt

package kr.co.ongil.data.datasource.remote.websocket

import android.util.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StompWebSocketClient @Inject constructor() {

    private var stompClient: StompClient? = null
    private val compositeDisposable = CompositeDisposable()

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    sealed class ConnectionState {
        data object Connected : ConnectionState()
        data object Connecting : ConnectionState()
        data object Disconnected : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }

    /**
     * WebSocket 연결
     */
    fun connect(baseUrl: String, accessToken: String) {
        Log.d("WebSocket", "연결 시작: $baseUrl")

        _connectionState.value = ConnectionState.Connecting

        // STOMP 클라이언트 생성
        val url = "${baseUrl}/api/ws"
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url).apply {
            // Authorization 헤더 추가
            val headers = listOf(
                StompHeader("Authorization", "Bearer $accessToken")
            )
            connect(headers)
        }

        // 연결 상태 구독
        val lifecycleDisposable = stompClient!!.lifecycle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { event ->
                when (event.type) {
                    LifecycleEvent.Type.OPENED -> {
                        Log.d("WebSocket", "연결 성공")
                        _connectionState.value = ConnectionState.Connected
                    }
                    LifecycleEvent.Type.ERROR -> {
                        Log.e("WebSocket", "연결 에러: ${event.exception}")
                        _connectionState.value = ConnectionState.Error(
                            event.exception?.message ?: "Unknown error"
                        )
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        Log.d("WebSocket", "연결 종료")
                        _connectionState.value = ConnectionState.Disconnected
                    }
                    LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                        Log.w("WebSocket", "Heartbeat 실패")
                    }
                }
            }

        compositeDisposable.add(lifecycleDisposable)
    }

    /**
     * 시그널 메시지 구독
     */
    fun subscribeToCallSignals(
        onMessage: (SignalMessageDto) -> Unit
    ) {
        val topic = "/user/queue/calls"

        val subscription = stompClient?.topic(topic)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { stompMessage ->
                    Log.d("WebSocket", "메시지 수신: ${stompMessage.payload}")

                    // JSON 파싱
                    val signal = parseSignalMessage(stompMessage.payload)
                    onMessage(signal)
                },
                { error ->
                    Log.e("WebSocket", "구독 에러", error)
                }
            )

        subscription?.let { compositeDisposable.add(it) }
    }

    /**
     * 시그널 메시지 전송
     */
    fun sendSignal(callId: Int, signal: SignalMessageDto) {
        val destination = "/app/calls/$callId/signal"
        val json = signal.toJson()

        stompClient?.send(destination, json)
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                {
                    Log.d("WebSocket", "메시지 전송 성공: $json")
                },
                { error ->
                    Log.e("WebSocket", "메시지 전송 실패", error)
                }
            )
            ?.let { compositeDisposable.add(it) }
    }

    /**
     * 연결 해제
     */
    fun disconnect() {
        stompClient?.disconnect()
        compositeDisposable.clear()
        _connectionState.value = ConnectionState.Disconnected
    }

    private fun parseSignalMessage(json: String): SignalMessageDto {
        // Gson 또는 Kotlinx Serialization 사용
        return Json.decodeFromString(json)
    }
}

data class StompHeader(val name: String, val value: String)
```

---

#### 3단계: SignalMessage DTO

**SignalMessageDto.kt** (신규 파일):
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/data/model/call/SignalMessageDto.kt

package kr.co.ongil.data.model.call

import kotlinx.serialization.Serializable

@Serializable
data class SignalMessageDto(
    val type: String,                // "INCOMING", "OFFER", "ANSWER", "ICE", "HANGUP"
    val sdp: String? = null,         // Session Description (OFFER/ANSWER)
    val candidate: String? = null,   // ICE candidate
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    val callId: Int,
    val fromUserId: Int,
    val toUserId: Int
) {
    fun toJson(): String {
        return Json.encodeToString(serializer(), this)
    }
}
```

---

#### 4단계: WebRTC Manager

**WebRTCManager.kt** (신규 파일):
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/service/call/WebRTCManager.kt

package kr.co.ongil.service.call

import android.content.Context
import android.util.Log
import io.getstream.webrtc.android.CreateSessionDescriptionObserver
import io.getstream.webrtc.android.SetSessionDescriptionObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    private val context: Context
) {

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    sealed class CallState {
        data object Idle : CallState()
        data object Connecting : CallState()
        data object Connected : CallState()
        data object Disconnected : CallState()
    }

    /**
     * WebRTC 초기화
     */
    fun initialize() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()

        PeerConnectionFactory.initialize(options)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(PeerConnectionFactory.Options())
            .createPeerConnectionFactory()

        Log.d("WebRTC", "PeerConnectionFactory 초기화 완료")
    }

    /**
     * PeerConnection 생성
     */
    fun createPeerConnection(
        iceServers: List<String>,         // TURN/STUN 서버 URL
        username: String? = null,
        credential: String? = null,
        onIceCandidate: (IceCandidate) -> Unit,
        onConnectionChange: (PeerConnection.PeerConnectionState) -> Unit
    ) {
        val iceServersList = iceServers.map { url ->
            PeerConnection.IceServer.builder(url)
                .setUsername(username ?: "")
                .setPassword(credential ?: "")
                .createIceServer()
        }

        val rtcConfig = PeerConnection.RTCConfiguration(iceServersList).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d("WebRTC", "ICE Candidate 생성: ${candidate.sdp}")
                    onIceCandidate(candidate)
                }

                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                    Log.d("WebRTC", "Connection State: $newState")
                    onConnectionChange(newState)

                    _callState.value = when (newState) {
                        PeerConnection.PeerConnectionState.CONNECTED -> CallState.Connected
                        PeerConnection.PeerConnectionState.CONNECTING -> CallState.Connecting
                        PeerConnection.PeerConnectionState.DISCONNECTED,
                        PeerConnection.PeerConnectionState.FAILED,
                        PeerConnection.PeerConnectionState.CLOSED -> CallState.Disconnected
                        else -> _callState.value
                    }
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                    Log.d("WebRTC", "ICE Connection State: $state")
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState) {
                    Log.d("WebRTC", "Signaling State: $state")
                }

                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
                override fun onAddStream(stream: MediaStream) {}
                override fun onRemoveStream(stream: MediaStream) {}
                override fun onDataChannel(channel: DataChannel) {}
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: RtpReceiver, streams: Array<out MediaStream>) {}
            }
        )

        // 로컬 오디오 트랙 추가
        addLocalAudioTrack()
    }

    /**
     * 로컬 오디오 트랙 추가
     */
    private fun addLocalAudioTrack() {
        val audioSource = peerConnectionFactory?.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory?.createAudioTrack("audio", audioSource)

        val mediaStream = peerConnectionFactory?.createLocalMediaStream("local_stream")
        mediaStream?.addTrack(localAudioTrack)

        peerConnection?.addStream(mediaStream)

        Log.d("WebRTC", "로컬 오디오 트랙 추가 완료")
    }

    /**
     * Offer 생성 (발신자)
     */
    fun createOffer(onSdpCreated: (SessionDescription) -> Unit) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : CreateSessionDescriptionObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SetSessionDescriptionObserver {
                    override fun onSetSuccess() {
                        Log.d("WebRTC", "Local SDP 설정 완료")
                        onSdpCreated(sdp)
                    }

                    override fun onSetFailure(error: String) {
                        Log.e("WebRTC", "Local SDP 설정 실패: $error")
                    }
                }, sdp)
            }

            override fun onCreateFailure(error: String) {
                Log.e("WebRTC", "Offer 생성 실패: $error")
            }
        }, constraints)
    }

    /**
     * Answer 생성 (수신자)
     */
    fun createAnswer(onSdpCreated: (SessionDescription) -> Unit) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createAnswer(object : CreateSessionDescriptionObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SetSessionDescriptionObserver {
                    override fun onSetSuccess() {
                        Log.d("WebRTC", "Local SDP (Answer) 설정 완료")
                        onSdpCreated(sdp)
                    }

                    override fun onSetFailure(error: String) {
                        Log.e("WebRTC", "Local SDP 설정 실패: $error")
                    }
                }, sdp)
            }

            override fun onCreateFailure(error: String) {
                Log.e("WebRTC", "Answer 생성 실패: $error")
            }
        }, constraints)
    }

    /**
     * Remote SDP 설정
     */
    fun setRemoteSdp(sdp: String, type: String) {
        val sessionType = if (type == "OFFER") {
            SessionDescription.Type.OFFER
        } else {
            SessionDescription.Type.ANSWER
        }

        val sessionDescription = SessionDescription(sessionType, sdp)

        peerConnection?.setRemoteDescription(object : SetSessionDescriptionObserver {
            override fun onSetSuccess() {
                Log.d("WebRTC", "Remote SDP 설정 완료")
            }

            override fun onSetFailure(error: String) {
                Log.e("WebRTC", "Remote SDP 설정 실패: $error")
            }
        }, sessionDescription)
    }

    /**
     * ICE Candidate 추가
     */
    fun addIceCandidate(candidate: String, sdpMid: String, sdpMLineIndex: Int) {
        val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)
        peerConnection?.addIceCandidate(iceCandidate)
        Log.d("WebRTC", "ICE Candidate 추가 완료")
    }

    /**
     * 통화 종료
     */
    fun close() {
        localAudioTrack?.dispose()
        peerConnection?.close()
        peerConnection = null
        _callState.value = CallState.Idle
        Log.d("WebRTC", "PeerConnection 종료")
    }

    /**
     * 리소스 해제
     */
    fun dispose() {
        close()
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
    }
}
```

---

#### 5단계: FCM 메시지 처리 (MyFirebaseMessagingService.kt 수정)

**기존 파일 수정**:
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/service/MyFirebaseMessagingService.kt

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var handleSosUseCase: HandleSosUseCase

    @Inject
    lateinit var stompWebSocketClient: StompWebSocketClient

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val type = data["type"]

        when (type) {
            "SOS" -> {
                // 기존 SOS 처리 로직
                handleSosMessage(data)
            }

            "INCOMING_CALL" -> {
                // ⭐ VoIP 통화 수신 처리
                handleIncomingCall(data)
            }

            else -> {
                Log.d("FCM", "알 수 없는 메시지 타입: $type")
            }
        }
    }

    private fun handleIncomingCall(data: Map<String, String>) {
        val callId = data["callId"]?.toIntOrNull() ?: return
        val sessionId = data["sessionId"] ?: return
        val callerId = data["callerId"]?.toIntOrNull() ?: return
        val callerName = data["callerName"] ?: return
        val callType = data["callType"] ?: "NORMAL"

        Log.d("FCM", "VoIP 통화 수신: callId=$callId, caller=$callerName")

        // 1. WebSocket 연결
        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = tokenManager.getAccessToken()
            if (accessToken != null) {
                stompWebSocketClient.connect("wss://your-domain.com", accessToken)
            }
        }

        // 2. 통화 화면 표시
        showIncomingCallNotification(callId, sessionId, callerId, callerName, callType)
    }

    private fun showIncomingCallNotification(
        callId: Int,
        sessionId: String,
        callerId: Int,
        callerName: String,
        callType: String
    ) {
        val intent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra("callId", callId)
            putExtra("sessionId", sessionId)
            putExtra("callerId", callerId)
            putExtra("callerName", callerName)
            putExtra("callType", callType)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, callId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, "voip_call")
            .setSmallIcon(R.drawable.ic_call)
            .setContentTitle("수신 전화")
            .setContentText("$callerName 님의 호출")
            .setCategory(Notification.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .addAction(
                R.drawable.ic_call_end,
                "거절",
                createRejectPendingIntent(callId)
            )
            .addAction(
                R.drawable.ic_call,
                "수락",
                pendingIntent
            )
            .build()

        getSystemService(NotificationManager::class.java)
            .notify(callId, notification)
    }

    private fun createRejectPendingIntent(callId: Int): PendingIntent {
        val intent = Intent(this, CallActionReceiver::class.java).apply {
            action = "REJECT_CALL"
            putExtra("callId", callId)
        }

        return PendingIntent.getBroadcast(
            this, callId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
```

---

#### 6단계: 통화 화면 (IncomingCallActivity.kt)

**신규 파일**:
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/presentation/ui/call/IncomingCallActivity.kt

package kr.co.ongil.presentation.ui.call

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import kr.co.ongil.presentation.theme.OngilTheme

@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {

    private val viewModel: IncomingCallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callId = intent.getIntExtra("callId", -1)
        val sessionId = intent.getStringExtra("sessionId") ?: ""
        val callerId = intent.getIntExtra("callerId", -1)
        val callerName = intent.getStringExtra("callerName") ?: ""
        val callType = intent.getStringExtra("callType") ?: "NORMAL"

        viewModel.initCall(callId, sessionId, callerId, callerName, callType)

        setContent {
            OngilTheme {
                IncomingCallScreen(
                    callerName = callerName,
                    callType = callType,
                    onAccept = { viewModel.acceptCall() },
                    onReject = { viewModel.rejectCall(); finish() },
                    callState = viewModel.callState.collectAsState().value
                )
            }
        }
    }
}

@Composable
fun IncomingCallScreen(
    callerName: String,
    callType: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    callState: CallState
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // 발신자 정보
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (callType == "EMERGENCY") "긴급 전화" else "수신 전화",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (callType == "EMERGENCY") {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = callerName,
                    style = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (callState) {
                        is CallState.Ringing -> "전화 오는 중..."
                        is CallState.Connecting -> "연결 중..."
                        is CallState.Connected -> "통화 중"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 통화 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 거절 버튼
                FloatingActionButton(
                    onClick = onReject,
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "거절"
                    )
                }

                // 수락 버튼
                FloatingActionButton(
                    onClick = onAccept,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "수락"
                    )
                }
            }
        }
    }
}
```

---

#### 7단계: IncomingCallViewModel

**신규 파일**:
```kotlin
// 파일 경로: app/src/main/java/kr/co/ongil/presentation/ui/call/IncomingCallViewModel.kt

package kr.co.ongil.presentation.ui.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kr.co.ongil.data.datasource.remote.websocket.StompWebSocketClient
import kr.co.ongil.data.model.call.SignalMessageDto
import kr.co.ongil.domain.repository.CallRepository
import kr.co.ongil.service.call.WebRTCManager
import javax.inject.Inject

@HiltViewModel
class IncomingCallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val webRTCManager: WebRTCManager,
    private val stompClient: StompWebSocketClient
) : ViewModel() {

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState

    private var callId: Int = -1
    private var sessionId: String = ""

    sealed class CallState {
        data object Idle : CallState()
        data object Ringing : CallState()
        data object Connecting : CallState()
        data object Connected : CallState()
        data object Ended : CallState()
    }

    fun initCall(
        callId: Int,
        sessionId: String,
        callerId: Int,
        callerName: String,
        callType: String
    ) {
        this.callId = callId
        this.sessionId = sessionId

        _callState.value = CallState.Ringing

        // WebSocket 시그널 구독
        subscribeToSignals()
    }

    private fun subscribeToSignals() {
        stompClient.subscribeToCallSignals { signal ->
            when (signal.type) {
                "OFFER" -> handleOffer(signal)
                "ICE" -> handleIceCandidate(signal)
                "HANGUP" -> handleHangup()
                else -> {}
            }
        }
    }

    /**
     * 통화 수락
     */
    fun acceptCall() {
        viewModelScope.launch {
            _callState.value = CallState.Connecting

            // 1. 백엔드에 통화 수락 전송
            callRepository.updateCallStatus(callId, "CONNECTED")

            // 2. WebSocket으로 ACCEPT 시그널 전송
            stompClient.sendSignal(
                callId,
                SignalMessageDto(
                    type = "ACCEPT",
                    callId = callId,
                    fromUserId = getCurrentUserId(),
                    toUserId = signal.fromUserId
                )
            )

            // 3. WebRTC 초기화 및 Answer 생성 대기
            initializeWebRTC()
        }
    }

    /**
     * 통화 거절
     */
    fun rejectCall() {
        viewModelScope.launch {
            // 1. 백엔드에 거절 전송
            callRepository.updateCallStatus(callId, "REJECTED")

            // 2. WebSocket으로 REJECT 시그널 전송
            stompClient.sendSignal(
                callId,
                SignalMessageDto(
                    type = "REJECT",
                    callId = callId,
                    fromUserId = getCurrentUserId(),
                    toUserId = -1  // 백엔드가 자동 계산
                )
            )

            _callState.value = CallState.Ended
        }
    }

    private fun initializeWebRTC() {
        // TURN/STUN 자격증명 가져오기
        viewModelScope.launch {
            val credentials = callRepository.getTurnCredentials()

            webRTCManager.initialize()
            webRTCManager.createPeerConnection(
                iceServers = credentials.urls,
                username = credentials.username,
                credential = credentials.credential,
                onIceCandidate = { candidate ->
                    // ICE candidate 전송
                    stompClient.sendSignal(
                        callId,
                        SignalMessageDto(
                            type = "ICE",
                            candidate = candidate.sdp,
                            sdpMid = candidate.sdpMid,
                            sdpMLineIndex = candidate.sdpMLineIndex,
                            callId = callId,
                            fromUserId = getCurrentUserId(),
                            toUserId = -1
                        )
                    )
                },
                onConnectionChange = { state ->
                    if (state == PeerConnection.PeerConnectionState.CONNECTED) {
                        _callState.value = CallState.Connected
                    }
                }
            )
        }
    }

    private fun handleOffer(signal: SignalMessageDto) {
        // Remote SDP 설정
        webRTCManager.setRemoteSdp(signal.sdp!!, "OFFER")

        // Answer 생성
        webRTCManager.createAnswer { sdp ->
            stompClient.sendSignal(
                callId,
                SignalMessageDto(
                    type = "ANSWER",
                    sdp = sdp.description,
                    callId = callId,
                    fromUserId = getCurrentUserId(),
                    toUserId = signal.fromUserId
                )
            )
        }
    }

    private fun handleIceCandidate(signal: SignalMessageDto) {
        webRTCManager.addIceCandidate(
            signal.candidate!!,
            signal.sdpMid!!,
            signal.sdpMLineIndex!!
        )
    }

    private fun handleHangup() {
        _callState.value = CallState.Ended
        webRTCManager.close()
    }

    override fun onCleared() {
        super.onCleared()
        webRTCManager.dispose()
        stompClient.disconnect()
    }

    private fun getCurrentUserId(): Int {
        // TokenManager에서 userId 가져오기
        return 1  // 임시
    }
}
```

---

## 📊 전체 플로우 요약

### 시스템 전화 (SYSTEM_DIALER)

```
1. 사용자가 "전화 걸기" 버튼 클릭
   ↓
2. SystemCallHelper.makeCall() - Intent.ACTION_CALL
   ↓
3. Android 기본 전화 앱 실행
   ↓
4. 통화 진행...
   ↓
5. 통화 종료 → CallStateReceiver 감지
   ↓
6. CallLog에서 통화 기록 조회
   ↓
7. POST /api/v1/calls/logs (백엔드로 전송)
   ↓
8. 백엔드 DB에 통화 기록 저장
```

### VoIP 통화 (APP)

#### 발신자 (Caller) 플로우

```
1. 발신자가 "VoIP 통화" 버튼 클릭
   ↓
2. POST /api/v1/calls { receiverId, callType }
   ↓
3. 백엔드:
   - Call 생성 (CREATED)
   - FCM 푸시 전송 (수신자 앱 깨우기)
   - WebSocket INCOMING 시그널
   - 상태: RINGING
   ↓
4. 발신자: WebSocket 연결 + TURN 자격증명 요청
   ↓
5. 발신자: WebRTC PeerConnection 생성
   ↓
6. 발신자: Offer 생성 → WebSocket으로 전송
   ↓
7. 수신자로부터 Answer 수신
   ↓
8. ICE candidate 교환
   ↓
9. P2P 연결 성공 → 통화 시작
```

#### 수신자 (Receiver) 플로우

```
1. FCM 푸시 수신 (type: "INCOMING_CALL")
   ↓
2. MyFirebaseMessagingService.onMessageReceived()
   ↓
3. WebSocket 연결
   ↓
4. Full-Screen Notification 표시
   ↓
5. IncomingCallActivity 실행
   ↓
6. 사용자가 "수락" 버튼 클릭
   ↓
7. PUT /api/v1/calls/{callId}/status { status: "CONNECTED" }
   ↓
8. WebSocket으로 ACCEPT 시그널 전송
   ↓
9. WebRTC PeerConnection 생성 + TURN 자격증명
   ↓
10. 발신자로부터 OFFER 수신
   ↓
11. Answer 생성 → WebSocket으로 전송
   ↓
12. ICE candidate 교환
   ↓
13. P2P 연결 성공 → 통화 시작
```

---

## 🛠️ 추가 구현 사항

### 1. CallApi.kt에 VoIP API 추가

```kotlin
interface CallApi {
    // 기존 메서드들...

    /**
     * VoIP 통화 생성
     */
    @POST("/api/v1/calls")
    suspend fun createCall(
        @Body request: CreateVoipCallRequest
    ): Response<CallResponse>

    /**
     * VoIP 통화 상태 업데이트
     */
    @PUT("/api/v1/calls/{callId}/status")
    suspend fun updateCallStatus(
        @Path("callId") callId: Int,
        @Body request: UpdateCallStatusRequest
    ): Response<CallResponse>

    /**
     * TURN/STUN 자격증명 조회
     */
    @GET("/api/v1/calls/rtc/turn-credentials")
    suspend fun getTurnCredentials(): Response<TurnCredentialsResponse>
}
```

### 2. DTO 모델 추가

```kotlin
@Serializable
data class CreateVoipCallRequest(
    val receiverId: Int,
    val callType: String  // "NORMAL" | "EMERGENCY"
)

@Serializable
data class UpdateCallStatusRequest(
    val status: String  // "RINGING", "CONNECTED", "ENDED", "REJECTED" 등
)

@Serializable
data class CallResponse(
    val id: Int,
    val caller: UserInfo,
    val receiver: UserInfo,
    val callType: String,
    val status: String,
    val sessionId: String,
    val startedAt: String,
    val connectedAt: String?,
    val endedAt: String?,
    val duration: Int?
)

@Serializable
data class TurnCredentialsResponse(
    val urls: List<String>,
    val username: String,
    val credential: String,
    val ttl: Long
)

@Serializable
data class UserInfo(
    val id: Int,
    val name: String,
    val phoneNumber: String
)
```

---

## 🔒 권한 설정 (AndroidManifest.xml)

```xml
<!-- 인터넷 -->
<uses-permission android:name="android.permission.INTERNET" />

<!-- 전화 -->
<uses-permission android:name="android.permission.CALL_PHONE" />
<uses-permission android:name="android.permission.READ_CALL_LOG" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />

<!-- 오디오 녹음 (WebRTC) -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

<!-- FCM -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Full-Screen Intent (Android 12+) -->
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
```

---

## ✅ 체크리스트

### 시스템 전화
- [ ] SystemCallHelper.kt 구현
- [ ] CallStateReceiver.kt 구현
- [ ] CallApi.createCallLog() 구현
- [ ] CallRepository에 createCallLog() 추가
- [ ] UI에서 전화 걸기 버튼 연동
- [ ] 권한 요청 처리

### VoIP 통화
- [ ] StompWebSocketClient.kt 구현
- [ ] WebRTCManager.kt 구현
- [ ] IncomingCallActivity.kt 구현
- [ ] IncomingCallViewModel.kt 구현
- [ ] MyFirebaseMessagingService.kt 수정 (INCOMING_CALL 처리)
- [ ] CallApi에 VoIP API 추가
- [ ] CallRepository에 VoIP 메서드 추가
- [ ] Notification Channel 생성
- [ ] 권한 요청 처리

---

## 📚 참고 자료

- [WebRTC 공식 문서](https://webrtc.org/)
- [Android Call Management](https://developer.android.com/guide/topics/connectivity/telecom)
- [STOMP Protocol](https://stomp.github.io/)
- [Firebase Cloud Messaging](https://firebase.google.com/docs/cloud-messaging)

---

## 🚀 다음 단계

1. **테스트**: 실제 2대의 Android 기기로 통화 테스트
2. **에러 핸들링**: 네트워크 끊김, 통화 실패 등 예외 처리
3. **UI/UX 개선**: 통화 중 화면, 음소거, 스피커 전환 등
4. **배터리 최적화**: Doze 모드 대응
5. **로깅**: 통화 품질 모니터링
