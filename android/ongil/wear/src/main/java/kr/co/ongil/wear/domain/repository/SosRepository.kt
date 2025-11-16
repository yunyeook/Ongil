package kr.co.ongil.wear.domain.repository

import kr.co.ongil.wear.domain.model.SosAlertResult

/**
 * SOS 알림 Repository Interface
 */
interface SosRepository {

    /**
     * SOS 알림 전송
     */
    suspend fun sendSosAlert(
        patientId: Int,
        latitude: Double,
        longitude: Double,
        message: String? = null
    ): Result<SosAlertResult>

    /**
     * SOS 알림 종료
     */
    suspend fun stopSosAlert(
        patientId: Int
    ): Result<String>
}
