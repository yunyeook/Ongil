package kr.co.ongil.presentation.uistate

/**
 * 휴대폰 인증 UI 상태
 */
enum class PhoneUiState {
    Idle,       // 초기 상태 (변경하기 버튼 표시)
    Editing,    // 새 번호 입력 중 (인증번호 발송 버튼 표시)
    Verifying   // 인증번호 입력 중 (재발송/확인 버튼 표시)
}

/**
 * 내 정보 수정 화면 UI 상태
 */
data class MyInfoEditUiState(
    val name: String = "",
    val birth: String = "",
    val phone: String = "",
    val profileImageUrl: String? = null,
    val roleLabel: String = "",

    // 휴대폰 인증 관련 상태
    val phoneUiState: PhoneUiState = PhoneUiState.Idle,
    val newPhone: String = "",
    val verificationCode: String = "",
    val verificationResult: Boolean? = null,  // null: 대기, true: 성공, false: 실패
    val secondsLeft: Int = 0,

    // 전역 상태
    val isLoading: Boolean = false,
    val error: String? = null
)

/**
 * 내 정보 수정 화면 이벤트
 */
sealed interface MyInfoEditEvent {
    // 기본 정보 수정
    data class UpdateName(val name: String) : MyInfoEditEvent
    data class UpdateBirth(val birth: String) : MyInfoEditEvent
    data object PickProfileImage : MyInfoEditEvent
    data object PickBirthDate : MyInfoEditEvent

    // 휴대폰 인증
    data object StartPhoneEdit : MyInfoEditEvent
    data class UpdateNewPhone(val phone: String) : MyInfoEditEvent
    data class SendVerificationCode(val phone: String) : MyInfoEditEvent
    data class ResendVerificationCode(val phone: String) : MyInfoEditEvent
    data class UpdateVerificationCode(val code: String) : MyInfoEditEvent
    data class VerifyCode(val phone: String, val code: String) : MyInfoEditEvent

    // 기타
    data object ChangePassword : MyInfoEditEvent
    data class SaveInfo(val name: String, val birth: String, val phone: String) : MyInfoEditEvent
}
