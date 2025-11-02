package kr.co.ongil.data.util

import kr.co.ongil.data.model.error.ApiException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * HTTP 응답 에러를 ApiException으로 변환하는 핸들러
 */
object ErrorHandler {

    /**
     * Exception을 ApiException으로 변환
     */
    fun handleException(throwable: Throwable): ApiException {
        return when (throwable) {
            is HttpException -> handleHttpException(throwable)
            is SocketTimeoutException -> ApiException.NetworkError("서버 응답 시간이 초과되었습니다.")
            is UnknownHostException -> ApiException.NetworkError("네트워크 연결을 확인해주세요.")
            is IOException -> ApiException.NetworkError("네트워크 연결이 불안정합니다.")
            is ApiException -> throwable
            else -> ApiException.Unknown(throwable.message ?: "알 수 없는 오류가 발생했습니다.")
        }
    }

    /**
     * HTTP 상태 코드별로 ApiException 변환
     */
    private fun handleHttpException(exception: HttpException): ApiException {
        val errorMessage = try {
            exception.response()?.errorBody()?.string() ?: exception.message()
        } catch (e: Exception) {
            exception.message()
        }

        return when (exception.code()) {
            400 -> ApiException.BadRequest(
                extractMessage(errorMessage) ?: "잘못된 요청입니다. 입력 형식을 다시 확인해주세요."
            )
            401 -> ApiException.Unauthorized(
                extractMessage(errorMessage) ?: "인증이 필요합니다. 토큰이 유효하지 않습니다."
            )
            403 -> ApiException.Forbidden(
                extractMessage(errorMessage) ?: "수정 권한이 없습니다."
            )
            404 -> ApiException.NotFound(
                extractMessage(errorMessage) ?: "요청한 사용자를 찾을 수 없습니다."
            )
            409 -> ApiException.Conflict(
                extractMessage(errorMessage) ?: "이미 사용 중인 전화번호입니다."
            )
            410 -> ApiException.Gone(
                extractMessage(errorMessage) ?: "토큰이 만료되었거나 이미 사용되었습니다."
            )
            422 -> ApiException.UnprocessableEntity(
                extractMessage(errorMessage) ?: "입력값이 유효하지 않습니다. 형식을 확인해주세요."
            )
            in 500..599 -> ApiException.InternalServerError(
                extractMessage(errorMessage) ?: "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            )
            else -> ApiException.Unknown(
                extractMessage(errorMessage) ?: "알 수 없는 오류가 발생했습니다. (${exception.code()})"
            )
        }
    }

    /**
     * 에러 응답에서 message 추출
     * JSON 형식: {"message": "에러 메시지"}
     */
    private fun extractMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null

        return try {
            // 간단한 JSON 파싱 (message 필드 추출)
            val messagePattern = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            messagePattern.find(errorBody)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}
