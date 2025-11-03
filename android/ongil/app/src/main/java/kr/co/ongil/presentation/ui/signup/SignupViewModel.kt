package kr.co.ongil.presentation.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 회원가입 화면 ViewModel
 * 사용자 입력 처리 및 회원가입 유효성 검증
 */
class SignupViewModel(
    // TODO: 실제 구현 시 Repository 주입 필요
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignupUiState())
    val uiState: StateFlow<SignupUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var remainingSeconds: Int = 0

    // 기본 정보 입력 처리 - 이름, 생일, 등등..
    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onBirthClick() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onSetBirth(birth: String) {
        _uiState.update { it.copy(birth = birth, showDatePicker = false) }
    }

    fun onDismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }


    // 전화번호 인증
    fun onPhoneChange(newPhone: String) {
        _uiState.update { it.copy(phoneNumber = newPhone) }
    }

    fun onRequestVerificationCode() {
        // TODO: 실제 구현 시 repository.sendVerificationCode() 호출
        // 재요청 시 기존 타이머 취소
        cancelVerificationTimer()

        remainingSeconds = 180 // 3분
        _uiState.update {
            it.copy(
                isCodeRequested = true,
                showTimerText = true,
                remainingTimeText = formatSeconds(remainingSeconds),
                verificationStatusMessage = "",
                isCodeVerified = false
            )
        }
        startVerificationTimer()
    }

    fun onVerificationCodeChange(newCode: String) {
        _uiState.update { it.copy(verificationCode = newCode) }
    }

    fun onVerifyCodeClick() {
        // TODO: 실제 구현 시 repository.verifyCode() 호출
        val currentCode = _uiState.value.verificationCode
        val isValid = currentCode == "123456"

        if (isValid) {
            // 성공 시 타이머 중단 및 메시지 반영
            cancelVerificationTimer()
            _uiState.update {
                it.copy(
                    isCodeVerified = true,
                    showTimerText = false,
                    verificationStatusMessage = "인증번호 등록에 성공했습니다."
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isCodeVerified = false,
                    verificationStatusMessage = "올바른 인증번호가 아닙니다."
                )
            }
        }
    }

    private fun startVerificationTimer() {
        countdownJob = viewModelScope.launch {
            while (remainingSeconds > 0) {
                delay(1_000)
                remainingSeconds -= 1
                _uiState.update {
                    it.copy(remainingTimeText = formatSeconds(remainingSeconds))
                }
            }
            // 시간 만료
            _uiState.update {
                it.copy(
                    showTimerText = false,
                    isCodeVerified = false,
                    verificationStatusMessage = "인증 시간이 만료되었습니다."
                )
            }
        }
    }

    private fun cancelVerificationTimer() {
        countdownJob?.cancel()
        countdownJob = null
    }

    private fun formatSeconds(total: Int): String {
        val m = total / 60
        val s = total % 60
        return String.format("%02d:%02d", m, s)
    }

    // 비밀번호
    fun onPasswordChange(newPassword: String) {
        _uiState.update {
            it.copy(
                password = newPassword,
                passwordMasked = maskPassword(newPassword)
            )
        }
    }

    fun onPasswordConfirmChange(newPassword: String) {
        _uiState.update {
            it.copy(
                passwordConfirm = newPassword,
                passwordConfirmMasked = maskPassword(newPassword)
            )
        }
    }

    fun onTogglePasswordVisible() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onTogglePasswordConfirmVisible() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    private fun maskPassword(password: String): String {
        if (password.isEmpty()) return ""
        return "•".repeat(password.length.coerceAtMost(10))
    }



    // 프로필 이미지 처리
    fun onProfileImageClick() {
        // 갤러리/카메라 선택은 화면에서 처리
    }

    fun setProfileImage(imageUrl: String) {
        _uiState.update { it.copy(profileImageUrl = imageUrl) }
    }

    // 회원 유형
    fun onSelectGuardian() {
        _uiState.update { it.copy(userType = UserType.GUARDIAN) }
    }

    fun onSelectPatient() {
        _uiState.update { it.copy(userType = UserType.PATIENT) }
    }

    // 회원가입
    fun onSubmitSignup() {
        viewModelScope.launch {
            val isValid = validateSignupInput()

            if (!isValid) {
                _uiState.update { it.copy(showErrorModal = true) }
                return@launch
            }

            // TODO: 실제 구현 시 repository.signup() 호출
            val success = performSignup()

            _uiState.update {
                it.copy(
                    showSuccessModal = success,
                    showErrorModal = !success
                )
            }
        }
    }

    private fun validateSignupInput(): Boolean {
        val state = _uiState.value

        // 전화번호 인증 확인
        if (!state.isCodeVerified) return false

        // 비밀번호 검증
        if (state.password.isBlank() || state.passwordConfirm.isBlank()) return false
        if (state.password != state.passwordConfirm) return false

        // 회원 유형 선택 확인
        if (state.userType == null) return false

        return true
    }

    private fun performSignup(): Boolean {
        // TODO: 실제 repository.signup() 구현
        // multipart/form-data로 서버에 전송
        // - provider: "LOCAL"
        // - name, birth, phoneNumber, password, userType, profileImage
        return true
    }


    // 모달 처리
    fun onDismissSuccessModal() {
        _uiState.update { it.copy(showSuccessModal = false) }
        // 성공 후 로그인 화면 이동은 UI에서 처리
    }

    fun onDismissErrorModal() {
        _uiState.update { it.copy(showErrorModal = false) }
    }

    override fun onCleared() {
        super.onCleared()
        cancelVerificationTimer()
    }
}