package kr.co.ongil.data.model.user

/**
 * 사용자 정보 조회 API 응답
 * GET /api/v1/users/me
 */
data class UserResponse(
    val message: String,
    val data: UserData
)

data class UserData(
    val user: UserDto
)
