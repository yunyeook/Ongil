package kr.co.ongil.presentation.ui.auth

data class LoginUiState (

    val phone : String = "",
    val password : String = "",
    val isLoading: Boolean = false,
    val isLoginEnabled: Boolean = false

)



