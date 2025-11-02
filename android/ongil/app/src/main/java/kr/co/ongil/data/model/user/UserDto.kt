package kr.co.ongil.data.model.user

/**
 * 사용자 정보 DTO
 * API 응답: /api/v1/users/me
 */
data class UserDto(
    val id: Int,
    val name: String,
    val birth: String,
    val phoneNumber: String,
    val userType: String,
    val profileImage: String?
)
