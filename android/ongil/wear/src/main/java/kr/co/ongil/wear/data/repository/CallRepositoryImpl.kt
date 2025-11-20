package kr.co.ongil.wear.data.repository

import android.util.Log
import kr.co.ongil.wear.data.datasource.sync.WearDataClient
import kr.co.ongil.wear.domain.model.CallState
import kr.co.ongil.wear.domain.model.CallStatus
import kr.co.ongil.wear.domain.model.TurnCredentials
import kr.co.ongil.wear.domain.repository.CallRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Call Repository 구현 (블루투스 모델)
 *
 * Watch → Phone → Server 경로로 통화 요청 전송
 * - Watch: WearDataClient로 Phone에 메시지 전송
 * - Phone: WearMessageListenerService에서 수신 → VoipCallViewModel 통합
 */
@Singleton
class CallRepositoryImpl @Inject constructor(
    private val wearDataClient: WearDataClient
) : CallRepository {

    companion object {
        private const val TAG = "CallRepositoryImpl"
    }

    /**
     * 통화 시작 (Phone으로 전송)
     *
     * @param targetUserId 상대방 사용자 ID
     * @param targetName 상대방 이름 (현재는 사용 안함, Phone에서 처리)
     * @param targetPhone 상대방 전화번호 (현재는 사용 안함, Phone에서 처리)
     */
    override suspend fun createCall(
        targetUserId: String,
        targetName: String,
        targetPhone: String
    ): Result<Long> {
        return try {
            // Phone으로 통화 시작 요청 전송
            val success = wearDataClient.startCall(targetUserId.toLong())

            if (success) {
                Log.d(TAG, "통화 시작 요청 전송 완료: targetUserId=$targetUserId")
                // Phone이 실제 callId를 생성하므로, 임시 ID 반환
                Result.success(0L)
            } else {
                Log.e(TAG, "통화 시작 요청 전송 실패")
                Result.failure(Exception("Failed to send call start request to Phone"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "통화 시작 요청 오류", e)
            Result.failure(e)
        }
    }

    /**
     * 통화 상태 업데이트 (Phone으로 전송)
     *
     * @param callId 통화 ID
     * @param status 통화 상태 (ACCEPTED, REJECTED, ENDED 등)
     */
    override suspend fun updateCallStatus(
        callId: Long,
        status: String
    ): Result<Unit> {
        return try {
            val success = when (status.uppercase()) {
                "ACCEPTED", "CONNECTED" -> wearDataClient.acceptCall(callId)
                "REJECTED", "DECLINED" -> wearDataClient.rejectCall(callId)
                "ENDED", "CANCELLED" -> wearDataClient.endCall()
                else -> {
                    Log.w(TAG, "알 수 없는 통화 상태: $status")
                    false
                }
            }

            if (success) {
                Log.d(TAG, "통화 상태 업데이트 전송 완료: callId=$callId, status=$status")
                Result.success(Unit)
            } else {
                Log.e(TAG, "통화 상태 업데이트 전송 실패")
                Result.failure(Exception("Failed to send call status update to Phone"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "통화 상태 업데이트 오류", e)
            Result.failure(e)
        }
    }

    /**
     * 통화 정보 조회 (블루투스 모델에서는 미지원)
     *
     * Watch는 서버에 직접 쿼리할 수 없음
     * Phone의 VoipCallViewModel이 Watch로 상태를 sync해야 함
     *
     * TODO: Phone → Watch 상태 sync 구현 필요
     */
    override suspend fun getCall(callId: Long): Result<CallState> {
        Log.w(TAG, "getCall() not supported in Bluetooth model")
        Log.w(TAG, "TODO: Phone should sync call state to Watch via DataLayer")

        // 임시로 IDLE 상태 반환
        return Result.success(
            CallState(
                callId = callId,
                targetUserId = "",
                targetName = "",
                status = CallStatus.IDLE
            )
        )
    }

    /**
     * TURN 자격 증명 조회 (블루투스 모델에서는 불필요)
     *
     * Phone이 WebRTC를 처리하므로 Watch는 TURN 자격 증명이 필요 없음
     */
    override suspend fun getTurnCredentials(): Result<TurnCredentials> {
        Log.w(TAG, "getTurnCredentials() not needed in Bluetooth model")
        Log.w(TAG, "Phone handles WebRTC, Watch only handles UI")

        // 임시로 빈 자격 증명 반환
        return Result.success(
            TurnCredentials(
                urls = emptyList(),
                username = "",
                credential = ""
            )
        )
    }
}
