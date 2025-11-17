package kr.co.ongil.domain.usecase.safezone

import android.util.Log
import kotlinx.coroutines.flow.first
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.domain.repository.SafeZoneRepository
import javax.inject.Inject

class UpdateAndSaveSafeZoneUseCase @Inject constructor(
    private val safeZoneRepository: SafeZoneRepository,
    private val userDataStoreManager: UserDataStoreManager
) {
    suspend operator fun invoke(
        patientId: Long,
        level1Distance: Int,
        level1Dwell: Int,
        level2Distance: Int,
        level2Dwell: Int,
        level3Distance: Int,
        level3Dwell: Int,
        pushEnabled: Boolean,
        autoCallEnabled: Boolean
    ): Result<Unit> {
        return try {
            Log.e("UpdateAndSaveSafeZone", "🔵 안전범위 업데이트 시작 - patientId: $patientId")
            Log.e("UpdateAndSaveSafeZone", "  - 1단계: ${level1Distance}m, ${level1Dwell}분")
            Log.e("UpdateAndSaveSafeZone", "  - 2단계: ${level2Distance}m, ${level2Dwell}분")
            Log.e("UpdateAndSaveSafeZone", "  - 3단계: ${level3Distance}m, ${level3Dwell}분")

            // first() 사용 - firstOrNull() 대신
            val result = safeZoneRepository.updateSafeZone(
                patientId = patientId,
                firstBoundary = level1Distance.toDouble(),
                secondBoundary = level2Distance.toDouble(),
                thirdBoundary = level3Distance.toDouble(),
                firstTime = level1Dwell,
                secondTime = level2Dwell,
                thirdTime = level3Dwell
            ).first()

            result.onSuccess { data ->
                Log.e("UpdateAndSaveSafeZone", "✅ API 업데이트 성공")

                userDataStoreManager.saveSafeZoneSettings(
                    patientId = patientId,
                    level1Distance = level1Distance,
                    level1Dwell = level1Dwell,
                    level2Distance = level2Distance,
                    level2Dwell = level2Dwell,
                    level3Distance = level3Distance,
                    level3Dwell = level3Dwell,
                    pushEnabled = pushEnabled,
                    autoCallEnabled = autoCallEnabled
                )
                Log.e("UpdateAndSaveSafeZone", "✅ DataStore 저장 완료")
                return Result.success(Unit)
            }.onFailure { error ->
                Log.e("UpdateAndSaveSafeZone", "❌ API 실패: ${error.message}")
                return Result.failure(error)
            }

            Result.failure(Exception("응답 없음"))
        } catch (e: Exception) {
            Log.e("UpdateAndSaveSafeZone", "❌ 예외 발생: ${e.message}", e)
            Result.failure(e)
        }
    }
}
