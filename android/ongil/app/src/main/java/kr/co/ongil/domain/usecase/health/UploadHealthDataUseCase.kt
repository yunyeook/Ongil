package kr.co.ongil.domain.usecase.health

import android.util.Log
import kr.co.ongil.domain.model.HealthData
import kr.co.ongil.domain.repository.HealthDataRepository
import javax.inject.Inject

/**
 * 건강 데이터 업로드 UseCase
 */
class UploadHealthDataUseCase @Inject constructor(
    private val healthDataRepository: HealthDataRepository
) {
    companion object {
        private const val TAG = "UploadHealthDataUseCase"
    }

    suspend operator fun invoke(
        patientId: Long,
        healthData: HealthData
    ): Result<String> {
        Log.d(TAG, "invoke() - 환자 ID: $patientId")
        return healthDataRepository.uploadHealthData(patientId, healthData)
    }
}