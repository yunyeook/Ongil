package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kr.co.ongil.data.datasource.remote.api.PatientInfoApi
import kr.co.ongil.data.model.patientinfo.PatientInfoDto
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.PatientInfoRepository
import javax.inject.Inject

class PatientInfoRepositoryImpl @Inject constructor(
    private val patientInfoApi: PatientInfoApi
) : PatientInfoRepository {

    companion object {
        private const val TAG = "PatientInfoRepositoryImpl"
    }

    override fun getPatientInfo(patientId: Int): Flow<Result<PatientInfoDto>> = flow {
        try {
            Log.d(TAG, "getPatientInfo() - API 호출 시작: patientId=$patientId")
            val response = patientInfoApi.getPatientInfo(patientId)
            Log.d(TAG, "getPatientInfo() - API 호출 성공: ${response.data}")
            emit(Result.success(response.data))
        } catch (e: Exception) {
            Log.e(TAG, "getPatientInfo() - API 호출 실패", e)
            val apiException = ErrorHandler.handleException(e)
            emit(Result.failure(apiException))
        }
    }
}
