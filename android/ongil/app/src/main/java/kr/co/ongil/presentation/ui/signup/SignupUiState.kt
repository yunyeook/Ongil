package kr.co.ongil.presentation.ui.signup

/**
 * 회원가입 화면 UI State
 */
data class SignupUiState(
    val profileImageUrl: String? = null,

    val name: String = "",
    val birth: String = "",
    val showDatePicker: Boolean = false,

    val phoneNumber: String = "",
    val isCodeRequested: Boolean = false,

    val verificationCode: String = "",
    val isCodeVerified: Boolean = false,

    val showTimerText: Boolean = false,
    val remainingTimeText: String = "",
    val verificationStatusMessage: String = "",

    val password: String = "",
    val passwordMasked: String = "",

    val passwordConfirm: String = "",
    val passwordConfirmMasked: String = "",

    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,

    val userType: UserType? = null,

    val showSuccessModal: Boolean = false,
    val showErrorModal: Boolean = false,
)

/**
 * 회원 유형
 */
enum class UserType {
    GUARDIAN,  // 보호자
    PATIENT    // 환자
}