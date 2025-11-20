package kr.co.ongil.presentation.ui.findpassword

import androidx.compose.runtime.Immutable

@Immutable
data class FindPasswordUiState(
    val phone: String = "",
    val isPhoneValid: Boolean = false,
    val isCodeRequested: Boolean = false,
    val code: String = "",
    val codeLength: Int = 6,
    val remainingSec: Int = 0,
    val codeVerifyStatus: CodeVerifyStatus = CodeVerifyStatus.Idle,
    val isResetEnabled: Boolean = false,
    val verificationToken: String? = null
)

enum class CodeVerifyStatus {
    Idle, Success, Error, Expired
}

sealed interface FindPasswordUiEvent {
    data object ClickBack : FindPasswordUiEvent
    data class ChangePhone(val input: String) : FindPasswordUiEvent
    data object ClickSendCode : FindPasswordUiEvent
    data object ClickResendCode : FindPasswordUiEvent
    data class ChangeCode(val input: String) : FindPasswordUiEvent
    data object ClickVerifyCode : FindPasswordUiEvent
    data object ClickResetPassword : FindPasswordUiEvent
    data object ClickGoLogin : FindPasswordUiEvent
    data object ClickHelp : FindPasswordUiEvent
}

sealed interface FindPasswordUiSideEffect {
    data class ShowToast(val message: String) : FindPasswordUiSideEffect
    data object NavigateResetPassword : FindPasswordUiSideEffect
    data object NavigateBack : FindPasswordUiSideEffect
    data object NavigateLogin : FindPasswordUiSideEffect
}