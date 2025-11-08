package kr.co.ongil.data.model.error

/**
 * API 호출 시 발생하는 커스텀 예외
 */
sealed class ApiException(
    message: String,
    val statusCode: Int? = null
) : Exception(message) {

    /**
     * 400 Bad Request - 잘못된 요청
     */
    class BadRequest(message: String = "잘못된 요청입니다. 입력 형식을 다시 확인해주세요.") :
        ApiException(message, 400)

    /**
     * 401 Unauthorized - 인증 필요
     */
    class Unauthorized(message: String = "인증이 필요합니다. 토큰이 유효하지 않습니다.") :
        ApiException(message, 401)

    /**
     * 403 Forbidden - 권한 없음
     */
    class Forbidden(message: String = "수정 권한이 없습니다.") :
        ApiException(message, 403)

    /**
     * 404 Not Found - 리소스 없음
     */
    class NotFound(message: String = "요청한 사용자를 찾을 수 없습니다.") :
        ApiException(message, 404)

    /**
     * 409 Conflict - 중복 데이터
     */
    class Conflict(message: String = "이미 사용 중인 전화번호입니다.") :
        ApiException(message, 409)

    /**
     * 410 Gone - 토큰 만료/사용됨
     */
    class Gone(message: String = "토큰이 만료되었거나 이미 사용되었습니다.") :
        ApiException(message, 410)

    /**
     * 422 Unprocessable Entity - 입력값 유효하지 않음
     */
    class UnprocessableEntity(message: String = "입력값이 유효하지 않습니다. 형식을 확인해주세요.") :
        ApiException(message, 422)

    /**
     * 500 Internal Server Error - 서버 오류
     */
    class InternalServerError(message: String = "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.") :
        ApiException(message, 500)

    /**
     * 네트워크 연결 오류
     */
    class NetworkError(message: String = "네트워크 연결을 확인해주세요.") :
        ApiException(message)

    /**
     * 알 수 없는 오류
     */
    class Unknown(message: String = "알 수 없는 오류가 발생했습니다.") :
        ApiException(message)
}