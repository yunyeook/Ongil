package kr.co.ongil.wear.data.datasource.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watch → Phone 메시지 전송 클라이언트
 *
 * 블루투스 모델:
 * - Watch는 MessageClient로 Phone에 메시지 전송
 * - Phone은 WearMessageListenerService에서 수신
 */
@Singleton
class WearDataClient @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "WearDataClient"

        // Message Paths (Phone의 WearMessageListenerService와 동일)
        const val PATH_LOCATION_UPDATE = "/location/update"
        const val PATH_CALL_START = "/call/start"
        const val PATH_CALL_END = "/call/end"
        const val PATH_CALL_ACCEPT = "/call/accept"
        const val PATH_CALL_REJECT = "/call/reject"
        const val PATH_SOS_ALERT = "/sos/alert"
        const val PATH_HELP_REQUEST = "/help/request"
    }

    private val messageClient: MessageClient = Wearable.getMessageClient(context)
    private val nodeClient: NodeClient = Wearable.getNodeClient(context)

    /**
     * 위치 업데이트를 Phone으로 전송
     *
     * @param latitude 위도
     * @param longitude 경도
     */
    suspend fun sendLocation(
        latitude: Double,
        longitude: Double
    ): Boolean {
        return try {
            val message = "$latitude,$longitude"
            sendMessage(PATH_LOCATION_UPDATE, message)
            Log.d(TAG, "위치 전송 성공: lat=$latitude, lon=$longitude")
            true
        } catch (e: Exception) {
            Log.e(TAG, "위치 전송 실패", e)
            false
        }
    }

    /**
     * 통화 시작 요청을 Phone으로 전송
     *
     * @param receiverId 상대방 사용자 ID
     */
    suspend fun startCall(receiverId: Long): Boolean {
        return try {
            val message = "receiver_id=$receiverId"
            sendMessage(PATH_CALL_START, message)
            Log.d(TAG, "통화 시작 요청 전송: receiverId=$receiverId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "통화 시작 요청 실패", e)
            false
        }
    }

    /**
     * 통화 종료 요청을 Phone으로 전송
     */
    suspend fun endCall(): Boolean {
        return try {
            sendMessage(PATH_CALL_END, "")
            Log.d(TAG, "통화 종료 요청 전송")
            true
        } catch (e: Exception) {
            Log.e(TAG, "통화 종료 요청 실패", e)
            false
        }
    }

    /**
     * 통화 수락을 Phone으로 전송
     *
     * @param callId 통화 ID
     */
    suspend fun acceptCall(callId: Long): Boolean {
        return try {
            val message = "call_id=$callId"
            sendMessage(PATH_CALL_ACCEPT, message)
            Log.d(TAG, "통화 수락 전송: callId=$callId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "통화 수락 실패", e)
            false
        }
    }

    /**
     * 통화 거절을 Phone으로 전송
     *
     * @param callId 통화 ID
     */
    suspend fun rejectCall(callId: Long): Boolean {
        return try {
            val message = "call_id=$callId"
            sendMessage(PATH_CALL_REJECT, message)
            Log.d(TAG, "통화 거절 전송: callId=$callId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "통화 거절 실패", e)
            false
        }
    }

    /**
     * SOS 알림을 Phone으로 전송
     *
     * @param latitude 현재 위도
     * @param longitude 현재 경도
     */
    suspend fun sendSos(
        latitude: Double,
        longitude: Double
    ): Boolean {
        return try {
            val message = "$latitude,$longitude"
            sendMessage(PATH_SOS_ALERT, message)
            Log.d(TAG, "SOS 알림 전송: lat=$latitude, lon=$longitude")
            true
        } catch (e: Exception) {
            Log.e(TAG, "SOS 알림 전송 실패", e)
            false
        }
    }

    /**
     * 도움 요청을 Phone으로 전송
     *
     * @param latitude 현재 위도
     * @param longitude 현재 경도
     * @param message 도움 요청 메시지
     */
    suspend fun sendHelpRequest(
        latitude: Double,
        longitude: Double,
        message: String
    ): Boolean {
        return try {
            val payload = "$latitude,$longitude,$message"
            sendMessage(PATH_HELP_REQUEST, payload)
            Log.d(TAG, "도움 요청 전송: $message")
            true
        } catch (e: Exception) {
            Log.e(TAG, "도움 요청 전송 실패", e)
            false
        }
    }

    /**
     * 메시지를 연결된 Phone 노드로 전송
     *
     * @param path 메시지 경로
     * @param message 메시지 내용
     */
    private suspend fun sendMessage(path: String, message: String) {
        // 연결된 노드 목록 가져오기
        val nodes: Set<Node> = nodeClient.connectedNodes.await()

        if (nodes.isEmpty()) {
            Log.w(TAG, "연결된 Phone 노드 없음")
            throw IllegalStateException("No connected phone nodes")
        }

        // 첫 번째 연결된 노드로 메시지 전송
        val node = nodes.first()
        messageClient.sendMessage(
            node.id,
            path,
            message.toByteArray()
        ).await()

        Log.d(TAG, "메시지 전송 성공: path=$path, nodeId=${node.id}")
    }
}
