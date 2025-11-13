package kr.co.ongil.domain.usecase.dashboard

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kr.co.ongil.data.model.dashboard.DashboardDto
import kr.co.ongil.domain.repository.DashboardRepository
import javax.inject.Inject

class GetDashboardUseCase @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    companion object {
        private const val TAG = "GetDashboardUseCase"
    }

    operator fun invoke(patientId: Int): Flow<Result<DashboardDto>> {
        Log.d(TAG, "invoke() - 홈 화면 요약 정보 조회: patientId=$patientId")
        return dashboardRepository.getDashboard(patientId)
    }
}
