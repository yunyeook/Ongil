package kr.co.ongil.data.model.user

import kotlinx.serialization.Serializable

/**
 * 사용자 정보 조회 API 응답
 * GET /api/v1/users/me
 *
 * 실제 서버 응답:
 * {
 *   "message": "...",
 *   "data": {
 *     "id": 1,
 *     "name": "홍길동",
 *     "birth": "19980919",
 *     "phoneNumber": "01012341234",
 *     "userType": "PATIENT",
 *     "profileImage": "S3_URL"
 *   }
 * }
 */
@Serializable
data class UserResponse(
    val message: String,
    val data: UserDto  // data 바로 아래에 유저 정보
)
