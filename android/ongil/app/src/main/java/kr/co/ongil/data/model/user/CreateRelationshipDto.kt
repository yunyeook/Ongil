package kr.co.ongil.data.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 관계 생성 요청
 * POST /api/v1/relationships/me
 *
 * 요청:
 * {
 *   "verificationToken": "eyJhbGciOi...",
 *   "relationshipName": "홍길동",
 *   "relationshipType": "자녀"
 * }
 */
@Serializable
data class CreateRelationshipRequest(
    @SerialName("verificationToken") val verificationToken: String,
    @SerialName("relationshipName") val relationshipName: String,
    @SerialName("relationshipType") val relationshipType: String
)

/**
 * 관계 생성 응답
 * POST /api/v1/relationships/me
 *
 * 응답:
 * {
 *   "message": "관계가 성공적으로 생성되었습니다.",
 *   "data": {
 *     "relationshipId": 1,
 *     "userId": 1,
 *     "counterpartUserId": 2,
 *     "relationshipName": "홍길동",
 *     "relationshipType": "자녀",
 *     "isDefault": false,
 *     "displayOrder": 0,
 *     "createdAt": "2025-01-01T00:00:00"
 *   }
 * }
 */
@Serializable
data class CreateRelationshipResponse(
    @SerialName("message") val message: String,
    @SerialName("data") val data: CreateRelationshipData
)

@Serializable
data class CreateRelationshipData(
    @SerialName("relationshipId") val relationshipId: Long,
    @SerialName("userId") val userId: Long,
    @SerialName("counterpartUserId") val counterpartUserId: Long,
    @SerialName("relationshipName") val relationshipName: String,
    @SerialName("relationshipType") val relationshipType: String,
    @SerialName("isDefault") val isDefault: Boolean,
    @SerialName("displayOrder") val displayOrder: Int,
    @SerialName("createdAt") val createdAt: String
)
