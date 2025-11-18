package kr.co.ongil.wear.data.repository

import android.util.Log
import kr.co.ongil.wear.data.datasource.sync.WearDataClient
import kr.co.ongil.wear.domain.model.SosAlertResult
import kr.co.ongil.wear.domain.repository.SosRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SOS Repository 구현 (블루투스 모델)
 *
 * Watch → Phone → Server 경로로 SOS 알림 전송
 * - Watch: WearDataClient로 Phone에 메시지 전송
 * - Phone: WearMessageListenerService에서 수신 → SosRepository로 서버 전송
 */
@Singleton
class SosRepositoryImpl @Inject constructor(
    private val wearDataClient: WearDataClient
) : SosRepository {

    companion object {
        private const val TAG = "SosRepositoryImpl"
    }

    /**
     * SOS 알림 전송 (Phone으로 전송)
     *
     * @param patientId 환자 ID (현재는 사용 안함, Phone에서 처리)
     * @param latitude 현재 위도
     * @param longitude 현재 경도
     * @param message 추가 메시지 (선택사항)
     */
    override suspend fun sendSosAlert(
        patientId: Int,
        latitude: Double,
        longitude: Double,
        message: String?
    ): Result<SosAlertResult> {
        return try {
            val success = if (message.isNullOrBlank()) {
                // 일반 SOS 알림 (위치만)
                wearDataClient.sendSos(latitude, longitude)
            } else {
                // 도움 요청 (위치 + 메시지)
                wearDataClient.sendHelpRequest(latitude, longitude, message)
            }

            if (success) {
                Log.d(TAG, "SOS 알림 전송 완료: lat=$latitude, lon=$longitude, message=$message")
                Result.success(
                    SosAlertResult(
                        success = true,
                        sosId = 0L, // Phone이 실제 sosId를 생성
                        message = "SOS alert sent to Phone"
                    )
                )
            } else {
                Log.e(TAG, "SOS 알림 전송 실패")
                Result.failure(Exception("Failed to send SOS alert to Phone"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "SOS 알림 전송 오류", e)
            Result.failure(e)
        }
    }

    /**
     * SOS 알림 중지 (Phone으로 전송)
     *
     * @param patientId 환자 ID
     *
     * TODO: WearDataClient에 stopSos() 메서드 추가 필요
     * 현재는 로컬에서만 처리
     */
    override suspend fun stopSosAlert(patientId: Int): Result<String> {
        return try {
            // TODO: WearDataClient에 stopSos() 메서드 추가
            Log.d(TAG, "SOS 알림 중지 (로컬 처리)")
            Log.w(TAG, "TODO: Phone relay for SOS stop not implemented yet")

            Result.success("SOS alert stopped (local only)")
        } catch (e: Exception) {
            Log.e(TAG, "SOS 알림 중지 오류", e)
            Result.failure(e)
        }
    }
}
