package kr.co.ongil.presentation.ui.patientdetail

import kotlinx.serialization.Serializable

@Serializable
data class PatientDetailUiState(
    val patientId: Long = -1L,
    val name: String = "",
    val phoneNumber: String = "",
    val gender: String = ""
)