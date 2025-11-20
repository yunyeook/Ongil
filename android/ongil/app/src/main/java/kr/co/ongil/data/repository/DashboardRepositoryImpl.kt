package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.ongil.data.datasource.remote.api.DashboardApi
import kr.co.ongil.data.model.dashboard.DashboardDto
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.DashboardRepository
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi
) : DashboardRepository {

    companion object {
        private const val TAG = "DashboardRepositoryImpl"
    }

    override fun getDashboard(patientId: Int): Flow<Result<DashboardDto>> = flow {
        try {
            Log.d(TAG, "getDashboard() - API 호출 시작: patientId=$patientId")
            val response = dashboardApi.getDashboard(patientId)
            Log.d(TAG, "getDashboard() - API 호출 성공: ${response.data}")
            emit(Result.success(response.data))
        } catch (e: Exception) {
            Log.e(TAG, "getDashboard() - API 호출 실패", e)
            val apiException = ErrorHandler.handleException(e)
            emit(Result.failure(apiException))
        }
    }
}
