package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * 비밀번호 변경 응답
 */
@Serializable
data class ChangePasswordResponse(
    val message: String,
    val data: JsonObject // API 명세상 data는 빈 객체 {}
)
