package kr.co.ongil.domain.usecase.sosalert

import kr.co.ongil.domain.repository.SosAlertRepository
import javax.inject.Inject

// SOS 알림 시작
class SendSosAlertUseCase @Inject constructor(
    private val sosAlertRepository: SosAlertRepository
) {
    suspend operator fun invoke(patientId: Int, message: String? = null): Result<Unit> {
        return sosAlertRepository.sendSosAlert(patientId, message)
    }
}

// SOS 알림 종료
class StopSosAlertUseCase @Inject constructor(
    private val sosAlertRepository: SosAlertRepository
) {
    suspend operator fun invoke(patientId: Int): Result<Unit> {
        return sosAlertRepository.stopSosAlert(patientId)
    }
}

// SOS 재생 완료 ACK
class AckSosAlertUseCase @Inject constructor(
    private val sosAlertRepository: SosAlertRepository
) {
    suspend operator fun invoke(sosId: Long): Result<Unit> {
        return sosAlertRepository.ackSosAlert(sosId)
    }
}
