package kr.co.ongil.presentation.ui.auth.register

/**
 * 회원가입 화면 UI State
 */
data class RegisterUiState(
    val profileImageUrl: String? = null,
    val profileImagePath: String? = null,

    val name: String = "",
    val birth: String? = null,
    val showDatePicker: Boolean = false,

    val phoneNumber: String = "",
    val isCodeRequested: Boolean = false,

    val verificationCode: String = "",  // 사용자가 입력하는 인증번호
    val verificationToken: String = "",  // 서버에서 받은 검증 토큰
    val isCodeVerified: Boolean = false,

    val showTimerText: Boolean = false,
    val remainingTimeText: String = "",
    val verificationStatusMessage: String = "",
    val remainingSeconds: Int = 0,
    val isTimerRunning: Boolean = false,

    val password: String = "",

    val passwordConfirm: String = "",

    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,

    // 유효성 검증
    val isNameValid: Boolean = true,
    val isBirthValid: Boolean = true,
    val isPhoneValid: Boolean = true,
    val isVerificationValid: Boolean = true,
    val isPasswordValid: Boolean = true,
    val isPasswordConfirmValid: Boolean = true,
    val isUserTypeValid: Boolean = true,

    // 제출 상태값
    val canSubmit: Boolean = false,
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false,

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