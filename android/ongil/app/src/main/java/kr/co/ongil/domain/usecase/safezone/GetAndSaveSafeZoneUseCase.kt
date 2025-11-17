package kr.co.ongil.domain.usecase.safezone

import android.util.Log
import kotlinx.coroutines.flow.first
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.domain.repository.SafeZoneRepository
import javax.inject.Inject

class GetAndSaveSafeZoneUseCase @Inject constructor(
    private val safeZoneRepository: SafeZoneRepository,
    private val userDataStoreManager: UserDataStoreManager
) {
    suspend operator fun invoke(patientId: Long): Result<Unit> {
        return try {

            val currentSettings = userDataStoreManager.getSafeZoneSettings(patientId)

            val result = safeZoneRepository.getSafeZone(patientId).first()

            result.onSuccess { data ->

                userDataStoreManager.saveSafeZoneSettings(
                    patientId = patientId,
                    level1Distance = data.boundaries.first.radius.toInt(),
                    level1Dwell = data.boundaries.first.time,
                    level2Distance = data.boundaries.second.radius.toInt(),
                    level2Dwell = data.boundaries.second.time,
                    level3Distance = data.boundaries.third.radius.toInt(),
                    level3Dwell = data.boundaries.third.time,
                    pushEnabled = currentSettings.pushEnabled,
                    autoCallEnabled = currentSettings.autoCallEnabled
                )
                return Result.success(Unit)
            }.onFailure { error ->
                Log.e("GetAndSaveSafeZone", "❌ API 실패: ${error.message}")
                return Result.failure(error)
            }

            Result.failure(Exception("응답 없음"))
        } catch (e: Exception) {
            Log.e("GetAndSaveSafeZone", "❌ 예외 발생: ${e.message}", e)
            Result.failure(e)
        }
    }
}
