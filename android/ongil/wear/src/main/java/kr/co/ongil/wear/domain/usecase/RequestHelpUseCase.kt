package kr.co.ongil.wear.domain.usecase

import android.util.Log
import kotlinx.coroutines.flow.first
import kr.co.ongil.common.location.LocationStreamBus
import kr.co.ongil.wear.data.datasource.local.WearDataStoreManager
import kr.co.ongil.wear.domain.repository.SosRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 도움 요청 UseCase
 *
 * 주요 기능:
 * 1. 현재 위치 가져오기
 * 2. Phone으로 도움 요청 전송
 * 3. Phone이 서버로 SOS 알림 전송
 *
 * TTS 음성 재생은 ViewModel에서 처리
 */
@Singleton
class RequestHelpUseCase @Inject constructor(
    private val sosRepository: SosRepository,
    private val locationStreamBus: LocationStreamBus,
    private val dataStoreManager: WearDataStoreManager
) {

    companion object {
        private const val TAG = "RequestHelpUseCase"
    }

    /**
     * 도움 요청 실행
     *
     * @param message 도움 요청 메시지 (예: "도와주세요! 길을 잃었습니다.")
     * @return 성공/실패 결과
     */
    suspend operator fun invoke(message: String): Result<Unit> {
        return try {
            Log.d(TAG, "도움 요청 시작: message=$message")

            // 1. 현재 위치 가져오기
            val location = locationStreamBus.lastKnownLocation ?: run {
                Log.w(TAG, "위치 정보 없음 - GPS를 켜주세요")
                return Result.failure(Exception("Location not available. Please enable GPS."))
            }

            Log.d(TAG, "현재 위치: lat=${location.latitude}, lon=${location.longitude}")

            // 2. 환자 ID 가져오기
            val userId = dataStoreManager.getUserId().first()
            val patientId = userId?.toIntOrNull() ?: run {
                Log.w(TAG, "환자 ID 없음 - 로그인이 필요합니다")
                return Result.failure(Exception("Patient ID not available. Please login."))
            }

            Log.d(TAG, "환자 ID: $patientId")

            // 3. SOS 알림 전송 (Watch → Phone → Server)
            val result = sosRepository.sendSosAlert(
                patientId = patientId,
                latitude = location.latitude,
                longitude = location.longitude,
                message = message
            )

            if (result.isSuccess) {
                Log.d(TAG, "✓ 도움 요청 전송 성공")
                Result.success(Unit)
            } else {
                val error = result.exceptionOrNull() ?: Exception("Failed to send help request")
                Log.e(TAG, "도움 요청 전송 실패: ${error.message}")
                Result.failure(error)
            }

        } catch (e: Exception) {
            Log.e(TAG, "도움 요청 중 오류 발생", e)
            Result.failure(e)
        }
    }
}
