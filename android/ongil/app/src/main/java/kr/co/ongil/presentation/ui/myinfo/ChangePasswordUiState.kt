package kr.co.ongil.presentation.uistate

/**
 * 비밀번호 변경 화면 UI 상태
 */
data class ChangePasswordUiState(
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",

    // 비밀번호 가시성
    val currentPasswordVisible: Boolean = false,
    val newPasswordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,

    // 비밀번호 재설정용
    val verificationToken: String? = null,

    // 전역 상태
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

/**
 * 비밀번호 변경 화면 이벤트
 */
sealed interface ChangePasswordEvent {
    // 입력 업데이트
    data class UpdateCurrentPassword(val password: String) : ChangePasswordEvent
    data class UpdateNewPassword(val password: String) : ChangePasswordEvent
    data class UpdateConfirmPassword(val password: String) : ChangePasswordEvent

    // 가시성 토글
    data object ToggleCurrentPasswordVisibility : ChangePasswordEvent
    data object ToggleNewPasswordVisibility : ChangePasswordEvent
    data object ToggleConfirmPasswordVisibility : ChangePasswordEvent

    // 비밀번호 재설정용
    data class SetVerificationToken(val token: String) : ChangePasswordEvent

    // 비밀번호 변경
    data object ChangePassword : ChangePasswordEvent
}
