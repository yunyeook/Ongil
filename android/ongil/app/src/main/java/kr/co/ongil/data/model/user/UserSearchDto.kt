package kr.co.ongil.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kr.co.ongil.domain.model.UserSummary

/**
 * 사용자 검색 요청
 * POST /api/v1/users/searches
 *
 * 요청:
 * {
 *   "phoneNumber": "01012345678"
 * }
 */
@Serializable
data class UserSearchRequest(
    @SerialName("phoneNumber") val phoneNumber: String
)

/**
 * 사용자 검색 응답
 * POST /api/v1/users/searches
 *
 * 응답:
 * {
 *   "message": "검색 결과가 성공적으로 조회되었습니다.",
 *   "data": {
 *     "user": {
 *       "id": 1,
 *       "name": "홍길동",
 *       "phoneNumber": "01012341234",
 *       "profileImage": "S3_URL"
 *     }
 *   }
 * }
 */
@Serializable
data class UserSearchResponse(
    @SerialName("message") val message: String,
    @SerialName("data") val data: UserSearchResponseData? = null
)

@Serializable
data class UserSearchResponseData(
    @SerialName("user") val user: UserSearchData
)

@Serializable
data class UserSearchData(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("phoneNumber") val phoneNumber: String,
    @SerialName("profileImage") val profileImage: String? = null
)

/**
 * DTO -> Domain 변환
 */
fun UserSearchResponse.toDomain(): UserSummary? {
    return data?.user?.let {
        UserSummary(
            id = it.id.toString(),
            displayName = it.name,
            phoneNumber = it.phoneNumber,
            avatarUrl = it.profileImage
        )
    }
}
