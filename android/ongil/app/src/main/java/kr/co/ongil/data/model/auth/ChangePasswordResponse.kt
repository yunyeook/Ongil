package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 비밀번호 변경 응답
 *
 * 실제 서버 응답:
 * {
 *   "message": "비밀번호가 성공적으로 변경되었습니다.",
 *   "data": ""
 * }
 */
@Serializable
data class ChangePasswordResponse(
    val message: String,
    val data: String  // 서버가 빈 문자열 "" 전송
)
