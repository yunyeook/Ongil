package kr.co.ongil.presentation.ui.favorite

import kotlinx.serialization.Serializable
import kr.co.ongil.data.model.favorite.RelationshipDto


@Serializable
data class PatientData(
    val id: Long,
    val relationshipId: Long,
    val name: String,
    val phoneNumber: String,
    val relationshipType: String,
    val profileImage: String? = null,
    val isDefault: Boolean = false
)


fun RelationshipDto.toPatientData(): PatientData {
    return PatientData(
        id = counterpartUserId,
        relationshipId = relationshipId,
        name = relationshipName ?: counterpartUser.name, // relationshipName이 null이면 실제 이름 사용
        phoneNumber = counterpartUser.phoneNumber,
        relationshipType = relationshipType,
        profileImage = counterpartUser.profileImage,
        isDefault = isDefault
    )
}