package kr.co.ongil.domain.usecase.health

import kr.co.ongil.domain.repository.HealthDataRepository
import javax.inject.Inject

/**
 * 건강 데이터 삭제 UseCase
 */
class DeleteHealthDataUseCase @Inject constructor(
    private val healthDataRepository: HealthDataRepository
) {
    /**
     * @param patientId 환자 ID
     * @param healthDataId 삭제할 건강 데이터 ID
     * @return Result<String> 성공 메시지 또는 에러
     */
    suspend operator fun invoke(
        patientId: Long,
        healthDataId: Long
    ): Result<String> {
        return healthDataRepository.deleteHealthData(
            patientId = patientId,
            healthDataId = healthDataId
        )
    }
}