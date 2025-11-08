package kr.co.ongil.presentation.ui.userdetail

import kotlinx.serialization.Serializable

@Serializable
data class UserDetailUiState(
    val relationshipId: Long = -1L,
    val patientId: Long = -1L,
    val name: String = "",
    val phoneNumber: String = "",
    val relationshipType: String = "",
    val userType: String = "", // PATIENT, GUARDIAN 등
    val isLoading: Boolean = false,
    val error: String? = null
)