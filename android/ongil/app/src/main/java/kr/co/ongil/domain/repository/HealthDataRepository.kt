package kr.co.ongil.domain.repository

import kr.co.ongil.data.model.health.HealthDataResponse
import kr.co.ongil.data.model.health.HealthSummaryResponse
import kr.co.ongil.domain.model.HealthData
import kr.co.ongil.domain.model.HealthDataType

/**
 * 헬스 데이터 원격 Repository 인터페이스
 */
interface HealthDataRepository {
    /**
     * 생체 데이터 업로드
     * @param patientId 환자 ID
     * @param healthData 업로드할 건강 데이터
     * @return Result<String> 성공 메시지 또는 에러
     */
    suspend fun uploadHealthData(patientId: Long, healthData: HealthData): Result<String>

    /**
     * 생체 데이터 조회
     * @param patientId 환자 ID
     * @param type 조회할 데이터 종류 (null이면 전체)
     * @param from 조회 시작 날짜 (yyyyMMdd)
     * @param to 조회 종료 날짜 (yyyyMMdd)
     * @param sort 정렬 기준 (기본: measuredAt,desc)
     * @return Result<HealthDataResponse> 건강 데이터 조회 결과
     */
    suspend fun getHealthData(
        patientId: Long,
        type: HealthDataType? = null,
        from: String? = null,
        to: String? = null,
        sort: String = "measuredAt,desc"
    ): Result<HealthDataResponse>

    /**
     * 생체 데이터 요약 통계 조회 (일별 단위)
     * @param patientId 환자 ID
     * @param type 통계할 데이터 종류 (null이면 전체)
     * @param from 조회 시작 날짜 (yyyyMMdd)
     * @param to 조회 종료 날짜 (yyyyMMdd)
     * @return Result<HealthSummaryResponse> 건강 데이터 요약 통계
     */
    suspend fun getHealthDataSummary(
        patientId: Long,
        type: HealthDataType? = null,
        from: String? = null,
        to: String? = null
    ): Result<HealthSummaryResponse>

    /**
     * 생체 데이터 삭제
     * @param patientId 환자 ID
     * @param healthDataId 삭제할 건강 데이터 ID
     * @return Result<String> 성공 메시지 또는 에러
     */
    suspend fun deleteHealthData(
        patientId: Long,
        healthDataId: Long
    ): Result<String>
}