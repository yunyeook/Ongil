package kr.co.ongil.service.wear

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kr.co.ongil.data.datasource.wear.WearDataClient
import kr.co.ongil.domain.repository.CallRepository
import kr.co.ongil.domain.repository.LocationRepository
import kr.co.ongil.domain.repository.SosRepository
import javax.inject.Inject

/**
 * Watch로부터 메시지를 수신하는 서비스
 *
 * 블루투스 모델:
 * - Watch는 MessageClient로 메시지 전송
 * - Phone은 이 서비스에서 수신 → 서버로 relay
 */
@AndroidEntryPoint
class WearMessageListenerService : WearableListenerService() {

    companion object {
        private const val TAG = "WearMessageListener"

        // Message Paths (Watch의 WearDataClient와 동일)
        private const val PATH_LOCATION_UPDATE = "/location/update"
        private const val PATH_CALL_START = "/call/start"
        private const val PATH_CALL_END = "/call/end"
        private const val PATH_CALL_ACCEPT = "/call/accept"
        private const val PATH_CALL_REJECT = "/call/reject"
        private const val PATH_SOS_ALERT = "/sos/alert"
        private const val PATH_HELP_REQUEST = "/help/request"
    }

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var callRepository: CallRepository

    @Inject
    lateinit var sosRepository: SosRepository

    @Inject
    lateinit var wearDataClient: WearDataClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        val path = messageEvent.path
        val data = String(messageEvent.data)

        Log.d(TAG, "메시지 수신: path=$path, data=$data")

        when (path) {
            PATH_LOCATION_UPDATE -> handleLocationUpdate(data)
            PATH_CALL_START -> handleCallStart(data)
            PATH_CALL_END -> handleCallEnd()
            PATH_CALL_ACCEPT -> handleCallAccept(data)
            PATH_CALL_REJECT -> handleCallReject(data)
            PATH_SOS_ALERT -> handleSosAlert(data)
            PATH_HELP_REQUEST -> handleHelpRequest(data)
            else -> Log.w(TAG, "Unknown message path: $path")
        }
    }

    /**
     * Watch로부터 위치 업데이트 수신 → 서버로 전송
     *
     * @param data "latitude,longitude" 형식
     */
    private fun handleLocationUpdate(data: String) {
        serviceScope.launch {
            try {
                val (latStr, lonStr) = data.split(",")
                val latitude = latStr.toDouble()
                val longitude = lonStr.toDouble()

                // 서버로 위치 전송
                locationRepository.updateLocation(latitude, longitude)

                Log.d(TAG, "위치 업데이트 처리 완료: lat=$latitude, lon=$longitude")
            } catch (e: Exception) {
                Log.e(TAG, "위치 업데이트 처리 실패", e)
            }
        }
    }

    /**
     * Watch로부터 통화 시작 요청 수신 → WebRTC 통화 시작
     *
     * @param data "receiver_id=123" 형식
     */
    private fun handleCallStart(data: String) {
        serviceScope.launch {
            try {
                val receiverId = data.split("=")[1].toLong()

                // TODO: VoipCallViewModel을 통해 WebRTC 통화 시작
                // voipCallViewModel.startVoipCall(receiverId, "PATIENT")

                Log.d(TAG, "통화 시작 요청 처리: receiverId=$receiverId")
                Log.w(TAG, "TODO: VoipCallViewModel 통합 필요")
            } catch (e: Exception) {
                Log.e(TAG, "통화 시작 요청 처리 실패", e)
            }
        }
    }

    /**
     * Watch로부터 통화 종료 요청 수신 → WebRTC 통화 종료
     */
    private fun handleCallEnd() {
        serviceScope.launch {
            try {
                // TODO: VoipCallViewModel을 통해 WebRTC 통화 종료
                // voipCallViewModel.endCall()

                Log.d(TAG, "통화 종료 요청 처리")
                Log.w(TAG, "TODO: VoipCallViewModel 통합 필요")
            } catch (e: Exception) {
                Log.e(TAG, "통화 종료 요청 처리 실패", e)
            }
        }
    }

    /**
     * Watch로부터 통화 수락 요청 수신
     *
     * @param data "call_id=456" 형식
     */
    private fun handleCallAccept(data: String) {
        serviceScope.launch {
            try {
                val callId = data.split("=")[1].toLong()

                // TODO: VoipCallViewModel을 통해 통화 수락
                // voipCallViewModel.acceptCall("PATIENT")

                Log.d(TAG, "통화 수락 요청 처리: callId=$callId")
                Log.w(TAG, "TODO: VoipCallViewModel 통합 필요")
            } catch (e: Exception) {
                Log.e(TAG, "통화 수락 요청 처리 실패", e)
            }
        }
    }

    /**
     * Watch로부터 통화 거절 요청 수신
     *
     * @param data "call_id=456" 형식
     */
    private fun handleCallReject(data: String) {
        serviceScope.launch {
            try {
                val callId = data.split("=")[1].toLong()

                // TODO: VoipCallViewModel을 통해 통화 거절
                // voipCallViewModel.rejectCall()

                Log.d(TAG, "통화 거절 요청 처리: callId=$callId")
                Log.w(TAG, "TODO: VoipCallViewModel 통합 필요")
            } catch (e: Exception) {
                Log.e(TAG, "통화 거절 요청 처리 실패", e)
            }
        }
    }

    /**
     * Watch로부터 SOS 알림 수신 → 서버로 전송
     *
     * @param data "latitude,longitude" 형식
     */
    private fun handleSosAlert(data: String) {
        serviceScope.launch {
            try {
                val (latStr, lonStr) = data.split(",")
                val latitude = latStr.toDouble()
                val longitude = lonStr.toDouble()

                // 서버로 SOS 알림 전송
                sosRepository.sendSos(latitude, longitude)

                Log.d(TAG, "SOS 알림 처리 완료: lat=$latitude, lon=$longitude")
            } catch (e: Exception) {
                Log.e(TAG, "SOS 알림 처리 실패", e)
            }
        }
    }

    /**
     * Watch로부터 도움 요청 수신 → 서버로 전송
     *
     * @param data "latitude,longitude,message" 형식
     */
    private fun handleHelpRequest(data: String) {
        serviceScope.launch {
            try {
                val parts = data.split(",", limit = 3)
                val latitude = parts[0].toDouble()
                val longitude = parts[1].toDouble()
                val message = parts[2]

                // 서버로 도움 요청 전송
                sosRepository.sendHelpRequest(latitude, longitude, message)

                Log.d(TAG, "도움 요청 처리 완료: $message")
            } catch (e: Exception) {
                Log.e(TAG, "도움 요청 처리 실패", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "WearMessageListenerService destroyed")
    }
}
