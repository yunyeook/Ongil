package kr.co.ongil.domain.repository

import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.safezone.SafeZoneData

/**
 * 안전범위 설정 Repository
 */
interface SafeZoneRepository {

    /**
     * 환자의 안전범위 조회
     *
     * @param patientId 환자 ID
     * @return 환자의 안전범위 데이터
     */
    fun getSafeZone(
        patientId: Long
    ): Flow<Result<SafeZoneData>>

    /**
     * 환자의 안전범위 수정
     *
     * @param patientId 환자 ID
     * @param firstBoundary 1단계 반경(미터)
     * @param secondBoundary 2단계 반경(미터)
     * @param thirdBoundary 3단계 반경(미터)
     * @param firstTime 1단계 이상탐지 시간(분)
     * @param secondTime 2단계 이상탐지 시간(분)
     * @param thirdTime 3단계 이상탐지 시간(분)
     * @return 수정된 안전범위 데이터
     */
    fun updateSafeZone(
        patientId: Long,
        firstBoundary: Double,
        secondBoundary: Double,
        thirdBoundary: Double,
        firstTime: Int,
        secondTime: Int,
        thirdTime: Int
    ): Flow<Result<SafeZoneData>>
}
