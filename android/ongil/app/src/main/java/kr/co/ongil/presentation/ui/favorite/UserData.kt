package kr.co.ongil.presentation.ui.favorite

import kotlinx.serialization.Serializable
import kr.co.ongil.data.model.favorite.RelationshipDto


@Serializable
data class PatientData(
    val id: Long,
    val relationshipId: Long,
    val name: String,
    val phoneNumber: String,
    val relationshipType: String
)


fun RelationshipDto.toPatientData(): PatientData {
    return PatientData(
        id = counterpartUserId,
        relationshipId = relationshipId,
        name = relationshipName,
        phoneNumber = counterpartUser.phoneNumber,
        relationshipType = relationshipType
    )
}