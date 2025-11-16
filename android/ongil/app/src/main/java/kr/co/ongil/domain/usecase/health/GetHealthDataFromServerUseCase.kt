package kr.co.ongil.domain.usecase.health

import kr.co.ongil.data.model.health.HealthDataResponse
import kr.co.ongil.domain.model.HealthDataType
import kr.co.ongil.domain.repository.HealthDataRepository
import javax.inject.Inject

/**
 * 서버로부터 건강 데이터 조회 UseCase
 */
class GetHealthDataFromServerUseCase @Inject constructor(
    private val healthDataRepository: HealthDataRepository
) {
    /**
     * @param patientId 환자 ID
     * @param type 조회할 데이터 종류 (null이면 전체 조회)
     * @param from 조회 시작 날짜 (yyyyMMdd)
     * @param to 조회 종료 날짜 (yyyyMMdd)
     * @param sort 정렬 기준 (기본: measuredAt,desc)
     * @return Result<HealthDataResponse> 건강 데이터 조회 결과
     */
    suspend operator fun invoke(
        patientId: Long,
        type: HealthDataType? = null,
        from: String? = null,
        to: String? = null,
        sort: String = "measuredAt,desc"
    ): Result<HealthDataResponse> {
        return healthDataRepository.getHealthData(
            patientId = patientId,
            type = type,
            from = from,
            to = to,
            sort = sort
        )
    }
}