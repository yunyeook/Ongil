package kr.co.ongil.data.repository

import android.util.Log
import kr.co.ongil.data.datasource.remote.api.CallApi
import kr.co.ongil.data.model.call.CallDetailDto
import kr.co.ongil.data.model.call.CallLogDto
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.CallRepository
import javax.inject.Inject

/**
 * 통화 Repository 구현체
 */
class CallRepositoryImpl @Inject constructor(
    private val callApi: CallApi
) : CallRepository {

    override suspend fun getCallLogs(): Result<List<CallLogDto>> {
        return try {
            Log.d("CallRepository", "통화 목록 조회 시작")

            // API 호출 (AuthInterceptor가 자동으로 Authorization 헤더 추가)
            val response = callApi.getCallLogs()

            Log.d("CallRepository", "응답 코드: ${response.code()}")
            Log.d("CallRepository", "응답 성공 여부: ${response.isSuccessful}")

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                Log.e("CallRepository", "에러 응답: $errorBody")
            }

            if (response.isSuccessful && response.body() != null) {
                val callLogs = response.body()?.data?.callLogs ?: emptyList()
                Log.d("CallRepository", "통화 목록 ${callLogs.size}개 조회 성공")
                Result.success(callLogs)
            } else {
                // 에러 응답 처리
                val exception = ErrorHandler.handleException(
                    retrofit2.HttpException(response)
                )
                Log.e("CallRepository", "통화 목록 조회 실패: ${exception.message}")
                Result.failure(exception)
            }
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            Log.e("CallRepository", "통화 목록 조회 예외 발생", e)
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun getCallDetail(callLogId: Long): Result<CallDetailDto> {
        return try {
            // API 호출 (AuthInterceptor가 자동으로 Authorization 헤더 추가)
            val response = callApi.getCallDetail(callLogId = callLogId)

            if (response.isSuccessful && response.body() != null) {
                val callDetail = response.body()?.data
                    ?: throw IllegalStateException("Response body is null")
                Result.success(callDetail)
            } else {
                // 에러 응답 처리
                val exception = ErrorHandler.handleException(
                    retrofit2.HttpException(response)
                )
                Result.failure(exception)
            }
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}
