package kr.co.ongil.presentation.ui.auth.register

sealed interface RegisterUiEvent {
    data class OnNameChange(val value: String) : RegisterUiEvent
    data class OnBirthChange(val value: String) : RegisterUiEvent
    data class OnPhoneNumberChange(val value: String) : RegisterUiEvent
    data class OnVerificationCodeChange(val value: String) : RegisterUiEvent
    data class OnPasswordChange(val value: String) : RegisterUiEvent
    data class OnPasswordConfirmChange(val value: String) : RegisterUiEvent
    data class OnUserTypeSelect(val value: UserType) : RegisterUiEvent
    data class OnProfileImagePathChange(val path: String?) : RegisterUiEvent

    data object OnRequestVerificationCode : RegisterUiEvent
    data object OnVerifyCode : RegisterUiEvent
    data object OnSubmit : RegisterUiEvent

    data object OnTogglePasswordVisibility : RegisterUiEvent
    data object OnToggleConfirmPasswordVisibility : RegisterUiEvent
    data object OnShowDatePicker : RegisterUiEvent
    data object OnHideDatePicker : RegisterUiEvent

    data object OnClearError : RegisterUiEvent
}