package kr.co.ongil.wear.domain.repository

import kr.co.ongil.wear.domain.model.LocationUpdate
import kr.co.ongil.wear.domain.model.NavigationLocation
import kr.co.ongil.wear.domain.model.NavigationState

/**
 * 위치 및 네비게이션 Repository Interface
 */
interface LocationRepository {

    /**
     * 환자 위치 업데이트 전송
     */
    suspend fun updatePatientLocation(
        patientId: Long,
        latitude: Double,
        longitude: Double
    ): Result<String>

    /**
     * 네비게이션 시작
     */
    suspend fun startNavigation(
        patientId: Long,
        startLocation: NavigationLocation,
        endLocation: NavigationLocation,
        initiatedBy: String
    ): Result<Long> // navigationId 반환

    /**
     * 네비게이션 종료
     */
    suspend fun endNavigation(
        navigationId: Long,
        endedBy: String
    ): Result<String>
}
