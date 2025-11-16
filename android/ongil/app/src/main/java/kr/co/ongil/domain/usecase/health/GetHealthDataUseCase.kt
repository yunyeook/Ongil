package kr.co.ongil.domain.usecase.health

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.health.LocalHealthData
import kr.co.ongil.domain.repository.HealthConnectRepository
import javax.inject.Inject

class GetHealthDataUseCase @Inject constructor(
    private val healthConnectRepository: HealthConnectRepository
) {
    companion object {
        private const val TAG = "GetHealthDataUseCase"
    }

    operator fun invoke(): Flow<Result<LocalHealthData>> {
        Log.d(TAG, "invoke() - 건강 데이터 조회 UseCase 실행")
        return healthConnectRepository.getHealthData()
    }
}
