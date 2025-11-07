package kr.co.ongil.data.model.favorite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RelationshipsResponseDto(
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: List<RelationshipDto>
)

@Serializable
data class RelationshipDto(
    @SerialName("relationshipId")
    val relationshipId: Long,
    @SerialName("userId")
    val userId: Long,
    @SerialName("counterpartUserId")
    val counterpartUserId: Long,
    @SerialName("relationshipName")
    val relationshipName: String,
    @SerialName("relationshipType")
    val relationshipType: String,
    @SerialName("isDefault")
    val isDefault: Boolean,
    @SerialName("displayOrder")
    val displayOrder: Int,
    @SerialName("createdAt")
    val createdAt: String // ISO8601 문자열 그대로 보관
)

@Serializable
data class RelationshipDetailDto(
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: RelationshipDto?
)