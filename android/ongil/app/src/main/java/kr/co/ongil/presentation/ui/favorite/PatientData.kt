package kr.co.ongil.presentation.ui.favorite

import kotlinx.serialization.Serializable
import kr.co.ongil.data.model.favorite.RelationshipDto


@Serializable
data class PatientData(
    val id: Long,
    val name: String,
    val phoneNumber: String
)


fun RelationshipDto.toPatientData(): PatientData {
    return PatientData(
        id = counterpartUserId,
        name = relationshipName,
        phoneNumber = ""
    )
}