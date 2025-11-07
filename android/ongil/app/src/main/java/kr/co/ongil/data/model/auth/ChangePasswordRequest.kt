package kr.co.ongil.data.model.auth

import kotlinx.serialization.Serializable

/**
 * 비밀번호 변경 요청
 */
@Serializable
data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String,
    val confirmPassword: String
)