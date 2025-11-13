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
    val verificationToken: String? = null,    // 인증 성공 시 받은 토큰
    val secondsLeft: Int = 0,

    // UI 트리거 상태 (1회성 이벤트)
    val showImagePicker: Boolean = false,
    val showDatePicker: Boolean = false,

    // 선택된 이미지 파일 (저장 전까지 임시 보관)
    val selectedImageUri: String? = null,

    // 전역 상태
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaveSuccess: Boolean = false  // 저장 성공 시 true
)

/**
 * 내 정보 수정 화면 이벤트
 */
sealed interface MyInfoEditEvent {
    // 기본 정보 수정
    data class UpdateName(val name: String) : MyInfoEditEvent
    data class UpdateBirth(val birth: String) : MyInfoEditEvent

    // 이미지 선택 관련
    data object PickProfileImage : MyInfoEditEvent
    data class OnImageSelected(val uri: String) : MyInfoEditEvent
    data object DismissImagePicker : MyInfoEditEvent

    // 날짜 선택 관련
    data object PickBirthDate : MyInfoEditEvent
    data class OnDateSelected(val date: String) : MyInfoEditEvent  // YYYYMMDD 형식
    data object DismissDatePicker : MyInfoEditEvent

    // 휴대폰 인증
    data object StartPhoneEdit : MyInfoEditEvent
    data class UpdateNewPhone(val phone: String) : MyInfoEditEvent
    data class SendVerificationCode(val phone: String) : MyInfoEditEvent
    data class ResendVerificationCode(val phone: String) : MyInfoEditEvent
    data class UpdateVerificationCode(val code: String) : MyInfoEditEvent
    data class VerifyCode(val phone: String, val code: String) : MyInfoEditEvent

    // 기타
    data object ChangePassword : MyInfoEditEvent
    data object SaveInfo : MyInfoEditEvent  // 파라미터 제거 - ViewModel에서 uiState 사용
}
