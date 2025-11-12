package kr.co.ongil.domain.repository

// SOS 알림
interface SosAlertRepository {
    suspend fun sendSosAlert(patientId: Int, message: String?): Result<Unit>
    suspend fun stopSosAlert(patientId: Int): Result<Unit>
    suspend fun ackSosAlert(sosId: Long): Result<Unit>
}
