package kr.co.ongil.presentation.ui.userdetail

import kotlinx.serialization.Serializable

@Serializable
data class UserDetailUiState(
    val patientId: Long = -1L,
    val name: String = "",
    val phoneNumber: String = "",
    val gender: String = ""
)