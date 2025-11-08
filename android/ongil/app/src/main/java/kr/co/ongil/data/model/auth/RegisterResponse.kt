package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 회원가입 응답
 *
 * 실제 서버 응답:
 * {
 *   "message": "회원가입이 성공적으로 완료되었습니다",
 *   "data" : {}
 * }
 */
@Serializable
data class RegisterResponse(
    val message: String,
    val data: JsonElement? = null  // 빈 객체 {} 처리
)