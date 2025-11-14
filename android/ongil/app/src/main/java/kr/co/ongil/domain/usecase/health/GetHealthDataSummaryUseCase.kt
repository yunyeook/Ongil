package kr.co.ongil.domain.usecase.health

import kr.co.ongil.data.model.health.HealthSummaryResponse
import kr.co.ongil.domain.model.HealthDataType
import kr.co.ongil.domain.repository.HealthDataRepository
import javax.inject.Inject

/**
 * 건강 데이터 요약 통계 조회 UseCase
 */
class GetHealthDataSummaryUseCase @Inject constructor(
    private val healthDataRepository: HealthDataRepository
) {
    /**
     * @param patientId 환자 ID
     * @param type 통계할 데이터 종류 (null이면 전체)
     * @param from 조회 시작 날짜 (yyyyMMdd)
     * @param to 조회 종료 날짜 (yyyyMMdd)
     * @return Result<HealthSummaryResponse> 건강 데이터 요약 통계
     */
    suspend operator fun invoke(
        patientId: Long,
        type: HealthDataType? = null,
        from: String? = null,
        to: String? = null
    ): Result<HealthSummaryResponse> {
        return healthDataRepository.getHealthDataSummary(
            patientId = patientId,
            type = type,
            from = from,
            to = to
        )
    }
}