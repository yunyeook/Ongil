package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.ongil.data.datasource.remote.api.PatientInsightApi
import kr.co.ongil.data.model.insight.PatientInsightDto
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.PatientInsightRepository
import javax.inject.Inject

class PatientInsightRepositoryImpl @Inject constructor(
    private val patientInsightApi: PatientInsightApi
) : PatientInsightRepository {

    companion object {
        private const val TAG = "PatientInsightRepositoryImpl"
    }

    override fun getLatestWeeklyInsight(patientId: Int): Flow<Result<PatientInsightDto>> = flow {
        try {
            Log.d(TAG, "getLatestWeeklyInsight() - API 호출 시작: patientId=$patientId")
            val response = patientInsightApi.getLatestWeeklyInsight(patientId)
            Log.d(TAG, "getLatestWeeklyInsight() - API 호출 성공: ${response.data}")
            emit(Result.success(response.data))
        } catch (e: Exception) {
            Log.e(TAG, "getLatestWeeklyInsight() - API 호출 실패", e)
            val apiException = ErrorHandler.handleException(e)
            emit(Result.failure(apiException))
        }
    }
}
