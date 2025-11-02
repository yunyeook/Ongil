package kr.co.ongil.data.model.user

import kotlinx.serialization.Serializable

/**
 * 사용자 정보 조회 API 응답
 * GET /api/v1/users/me
 */
@Serializable
data class UserResponse(
    val message: String,
    val data: UserData
)

@Serializable
data class UserData(
    val user: UserDto
)
