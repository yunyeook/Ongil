package kr.co.ongil.data.repository

import android.util.Log
import kr.co.ongil.data.datasource.remote.api.HealthDataApi
import kr.co.ongil.data.mapper.toUploadRequest
import kr.co.ongil.data.model.health.HealthDataResponse
import kr.co.ongil.data.model.health.HealthSummaryResponse
import kr.co.ongil.domain.model.HealthData
import kr.co.ongil.domain.model.HealthDataType
import kr.co.ongil.domain.repository.HealthDataRepository
import javax.inject.Inject

/**
 * 헬스 데이터 원격 Repository 구현체
 */
class HealthDataRepositoryImpl @Inject constructor(
    private val healthDataApi: HealthDataApi
) : HealthDataRepository {

    companion object {
        private const val TAG = "HealthDataRepository"
    }

    override suspend fun uploadHealthData(
        patientId: Long,
        healthData: HealthData
    ): Result<String> = runCatching {
        Log.d(TAG, "uploadHealthData() - 환자 ID: $patientId, 데이터: $healthData")

        // 도메인 모델 → API 요청 모델 변환
        val request = healthData.toUploadRequest()
        Log.d(TAG, "uploadHealthData() - 업로드할 레코드 수: ${request.records.size}")

        // API 호출
        val response = healthDataApi.uploadHealthData(patientId, request)
        Log.d(TAG, "uploadHealthData() - 업로드 성공: ${response.message}")

        response.message
    }.onFailure { exception ->
        Log.e(TAG, "uploadHealthData() - 업로드 실패", exception)
    }

    override suspend fun getHealthData(
        patientId: Long,
        type: HealthDataType?,
        from: String?,
        to: String?,
        sort: String
    ): Result<HealthDataResponse> = runCatching {
        Log.d(TAG, "getHealthData() - 환자 ID: $patientId, type: $type, from: $from, to: $to, sort: $sort")

        // API 호출
        val response = healthDataApi.getHealthData(
            patientId = patientId,
            type = type?.value,
            from = from,
            to = to,
            sort = sort
        )

        Log.d(TAG, "getHealthData() - 조회 성공: ${response.data.records.size}개 레코드")
        response
    }.onFailure { exception ->
        Log.e(TAG, "getHealthData() - 조회 실패", exception)
    }

    override suspend fun getHealthDataSummary(
        patientId: Long,
        type: HealthDataType?,
        from: String?,
        to: String?
    ): Result<HealthSummaryResponse> = runCatching {
        Log.d(TAG, "getHealthDataSummary() - 환자 ID: $patientId, type: $type, from: $from, to: $to")

        // API 호출
        val response = healthDataApi.getHealthDataSummary(
            patientId = patientId,
            type = type?.value,
            from = from,
            to = to
        )

        Log.d(TAG, "getHealthDataSummary() - 조회 성공: ${response.data.summary.size}개 일별 요약")
        response
    }.onFailure { exception ->
        Log.e(TAG, "getHealthDataSummary() - 조회 실패", exception)
    }

    override suspend fun deleteHealthData(
        patientId: Long,
        healthDataId: Long
    ): Result<String> = runCatching {
        Log.d(TAG, "deleteHealthData() - 환자 ID: $patientId, 건강 데이터 ID: $healthDataId")

        // API 호출
        val response = healthDataApi.deleteHealthData(
            patientId = patientId,
            healthDataId = healthDataId
        )

        Log.d(TAG, "deleteHealthData() - 삭제 성공: ${response.message}")
        response.message
    }.onFailure { exception ->
        Log.e(TAG, "deleteHealthData() - 삭제 실패", exception)
    }
}