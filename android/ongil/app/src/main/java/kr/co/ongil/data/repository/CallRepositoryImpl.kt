package kr.co.ongil.data.repository

import kr.co.ongil.data.datasource.remote.api.CallApi
import kr.co.ongil.data.model.call.CallDetailDto
import kr.co.ongil.data.model.call.CallLogDto
import kr.co.ongil.data.util.ErrorHandler
import kr.co.ongil.domain.repository.CallRepository

/**
 * 통화 Repository 구현체
 */
class CallRepositoryImpl(
    private val callApi: CallApi? = null
    // TODO: TokenManager 추가하여 accessToken 자동으로 가져오기
    // TODO: DI(Hilt/Koin)로 주입하도록 변경
) : CallRepository {

    override suspend fun getCallLogs(): Result<List<CallLogDto>> {
        return try {
            // TODO: TokenManager에서 accessToken 가져오기
            val accessToken = "Bearer YOUR_ACCESS_TOKEN"

            if (callApi == null) {
                throw IllegalStateException("CallApi가 주입되지 않았습니다. DI를 통해 주입해주세요.")
            }

            // API 호출
            val response = callApi.getCallLogs(accessToken = accessToken)

            Result.success(response.data.callLogs)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }

    override suspend fun getCallDetail(callLogId: Long): Result<CallDetailDto> {
        return try {
            // TODO: TokenManager에서 accessToken 가져오기
            val accessToken = "Bearer YOUR_ACCESS_TOKEN"

            if (callApi == null) {
                throw IllegalStateException("CallApi가 주입되지 않았습니다. DI를 통해 주입해주세요.")
            }

            // API 호출
            val response = callApi.getCallDetail(
                accessToken = accessToken,
                callLogId = callLogId
            )

            Result.success(response.data)
        } catch (e: Exception) {
            // HTTP 에러를 ApiException으로 변환
            val apiException = ErrorHandler.handleException(e)
            Result.failure(apiException)
        }
    }
}
