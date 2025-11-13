package kr.co.ongil.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kr.co.ongil.data.model.fcm.FcmPayloadDto
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kr.co.ongil.data.mapper.FcmPayloadMapper
import kr.co.ongil.domain.model.FcmMessage
import kr.co.ongil.domain.model.MessageType
import kr.co.ongil.domain.usecase.fcm.HandleSosUseCase
import kr.co.ongil.domain.usecase.fcm.HandleSosStopUseCase
import kr.co.ongil.domain.usecase.fcm.HandleSosAckUseCase
import kr.co.ongil.domain.usecase.fcm.HandleRelationshipRegistUseCase
import kr.co.ongil.domain.usecase.fcm.HandleSafezoneExitUseCase
import kr.co.ongil.domain.usecase.fcm.HandleNavigationStartUseCase
import kr.co.ongil.domain.usecase.fcm.HandleNavigationEndUseCase
import kr.co.ongil.domain.usecase.fcm.HandleAbnormalDetectedUseCase
import kr.co.ongil.domain.usecase.fcm.HandleCallRequestUseCase
import kr.co.ongil.domain.helper.NotificationHelper
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var handleSosUseCase: HandleSosUseCase

    @Inject
    lateinit var handleSosStopUseCase: HandleSosStopUseCase

    @Inject
    lateinit var handleSosAckUseCase: HandleSosAckUseCase

    @Inject
    lateinit var handleRelationshipRegistUseCase: HandleRelationshipRegistUseCase

    @Inject
    lateinit var handleSafezoneExitUseCase: HandleSafezoneExitUseCase

    @Inject
    lateinit var handleNavigationStartUseCase: HandleNavigationStartUseCase

    @Inject
    lateinit var handleNavigationEndUseCase: HandleNavigationEndUseCase

    @Inject
    lateinit var handleAbnormalDetectedUseCase: HandleAbnormalDetectedUseCase

    @Inject
    lateinit var handleCallRequestUseCase: HandleCallRequestUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        // 알림 채널 생성
        notificationHelper.createNotificationChannels(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "🔄 FCM 토큰 갱신됨: $token")
        // 토큰은 로그인 시 서버로 전송됨 (LoginViewModel에서 처리)
        // 필요시 로컬에 저장하여 다음 로그인 때 사용 가능
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM", "📩 수신된 메시지: ${remoteMessage.data}")

        if (remoteMessage.data.isNotEmpty()) {

            val payloadDto = convertMapToDto(remoteMessage.data)

            val fcmMessage = FcmPayloadMapper.toDomain(payloadDto)

            when (fcmMessage.type) {
                MessageType.SOS -> {
                    handleSosUseCase(this, fcmMessage)
                }

                MessageType.SOS_STOP -> {
                    handleSosStopUseCase(this, fcmMessage)
                }

                MessageType.SOS_ACK -> {
                    handleSosAckUseCase(this, fcmMessage)
                }

                MessageType.INCOMING_CALL -> {
                    // VoIP 수신 통화 처리
                    handleIncomingCall(remoteMessage.data)
                }

                MessageType.RELATIONSHIP_REGIST -> {
                    handleRelationshipRegistUseCase(this, fcmMessage)
                }

                MessageType.SAFEZONE_EXIT -> {
                    handleSafezoneExitUseCase(this, fcmMessage)
                }

                MessageType.NAVIGATION_START -> {
                    handleNavigationStartUseCase(this, fcmMessage)
                }

                MessageType.NAVIGATION_END -> {
                    handleNavigationEndUseCase(this, fcmMessage)
                }

                MessageType.ABNORMAL_DETECTED -> {
                    handleAbnormalDetectedUseCase(this, fcmMessage)
                }

                MessageType.CALL_REQUEST -> {
                    handleCallRequestUseCase(this, fcmMessage)
                }
            }
        }
    }

    private fun convertMapToDto(data: Map<String, String>): FcmPayloadDto {
        val jsonObject = Gson().toJsonTree(data)
        val payloadDto = Gson().fromJson(jsonObject, FcmPayloadDto::class.java)
        return payloadDto
    }

    /**
     * VoIP 수신 통화 처리
     */
    private fun handleIncomingCall(data: Map<String, String>) {
        Log.d("FCM", "📞 VoIP 수신 통화 처리")

        val callId = data["callId"]?.toLongOrNull() ?: run {
            Log.e("FCM", "callId is null or invalid")
            return
        }

        val sessionId = data["sessionId"]
        val callerName = data["callerName"] ?: "알 수 없음"
        val callerPhone = data["callerPhone"] ?: ""
        val userType = data["userType"] ?: "PATIENT"   // 👈 추가 (GUARDIAN이면 그 값 들어오게)

        Log.d("FCM", "callId: $callId, sessionId: $sessionId, caller: $callerName")

        val intent = android.content.Intent(
            this,
            kr.co.ongil.presentation.MainActivity::class.java
        ).apply {
            addFlags(
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                        android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP // 👈 이거 추가
            )
            putExtra("type", "INCOMING_CALL")
            putExtra("callId", callId)
            putExtra("sessionId", sessionId)
            putExtra("callerName", callerName)
            putExtra("callerPhone", callerPhone)
            putExtra("userType", userType) // 👈 이걸로 NavGraph 쪽 VoipIncomingCall로 전달
        }

        startActivity(intent)
        Log.d("FCM", "✓ MainActivity로 이동 (INCOMING_CALL)")
    }

    /**
     * 알림을 처리하고 타겟에 따라 분기
     */
//    private fun handleNotification(data: FcmMessage) {
//        Log.d("FCM", "알림 타입: ${data.type}")
//        Log.d("FCM", "보낸 사람: ${data.senderId}, 받는 사람: ${data.receiverId}")
//
//        // type에 따라 분기 처리
//        val watchConnected = isWatchConnected();
//
//        // 워치가 연결되어 있다면 항상 워치로 알림을 보냅니다.
//        if (watchConnected) {
//            sendNotificationToWatch(data);
//        }
//
//        // 휴대폰 알림은 'SOS'이면서 워치가 연결된 경우'만' 제외하고 항상 보냅니다.
//        if (data.type !== "SOS" || !watchConnected) {
//            sendNotificationToPhone(data);
//        }
//    }
//
//    /**
//     * 폰에 알림 표시
//     */
//    private fun sendNotificationToPhone(data: FcmData) {
//        val intent = Intent(this, MainActivity::class.java).apply {
//            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        }
//
//        val pendingIntent = PendingIntent.getActivity(
//            this, 0, intent,
//            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val channelId = "default_channel"
//        val builder = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle(title)
//            .setContentText(messageBody)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//
//        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId, "기본 알림 채널", NotificationManager.IMPORTANCE_DEFAULT
//            )
//            manager.createNotificationChannel(channel)
//        }
//        manager.notify(0, builder.build())
//        Log.d("FCM", "✅ 폰 알림 전송 완료")
//    }
//
//    /**
//     * 워치로 메시지 전송 (Wear OS Data Layer API 사용)
//     */
//    private fun sendNotificationToWatch(data: FcmData) {
//        // TODO: Wear OS Data Layer API 또는 Message API 구현
//        // 예시:
//        // val messageClient = Wearable.getMessageClient(this)
//        // val message = JSONObject().apply {
//        //     put("title", title)
//        //     put("body", body)
//        //     put("type", type)
//        // }.toString()
//        //
//        // getWatchNodeIds().forEach { nodeId ->
//        //     messageClient.sendMessage(nodeId, "/notification", message.toByteArray())
//        // }
//
//        Log.d("FCM", "📱 워치로 메시지 전송: $title")
//        // 실제 구현은 Wear 모듈과 통신 설정 필요
//    }
//
//    /**
//     * 워치 연결 상태 확인
//     */
//    private fun isWatchConnected(): Boolean {
//        // TODO: Wear OS Node API로 연결된 워치 확인
//        // 예시:
//        // val nodeClient = Wearable.getNodeClient(this)
//        // val nodes = Tasks.await(nodeClient.connectedNodes)
//        // return nodes.isNotEmpty()
//
//        // 임시: 항상 false 반환 (워치 연동 전)
//        Log.d("FCM", "⌚ 워치 연결 상태 확인 (미구현)")
//        return false
//    }
}
