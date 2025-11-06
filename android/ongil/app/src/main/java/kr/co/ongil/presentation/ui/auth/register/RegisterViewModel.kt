package kr.co.ongil.presentation.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.domain.usecase.auth.RegisterUseCase
import kr.co.ongil.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import retrofit2.HttpException

// 회원가입
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun onEvent(e: RegisterUiEvent) {
        when (e) {
            is RegisterUiEvent.OnNameChange -> onNameChange(e.value)
            is RegisterUiEvent.OnBirthChange -> onSetBirth(e.value)
            is RegisterUiEvent.OnPhoneNumberChange -> onPhoneChange(e.value)
            is RegisterUiEvent.OnVerificationCodeChange -> onVerificationCodeChange(e.value)
            is RegisterUiEvent.OnPasswordChange -> onPasswordChange(e.value)
            is RegisterUiEvent.OnPasswordConfirmChange -> onPasswordConfirmChange(e.value)
            is RegisterUiEvent.OnUserTypeSelect -> setUserType(e.value)
            is RegisterUiEvent.OnProfileImagePathChange -> setProfileImage(e.path)

            RegisterUiEvent.OnRequestVerificationCode -> onRequestVerificationCode()
            RegisterUiEvent.OnVerifyCode -> onVerifyTokenClick()
            RegisterUiEvent.OnSubmit -> onSubmitRegister()

            RegisterUiEvent.OnTogglePasswordVisibility -> onTogglePasswordVisible()
            RegisterUiEvent.OnToggleConfirmPasswordVisibility -> onTogglePasswordConfirmVisible()
            RegisterUiEvent.OnShowDatePicker -> onBirthClick()
            RegisterUiEvent.OnHideDatePicker -> onDismissDatePicker()

            RegisterUiEvent.OnClearError -> _uiState.update { it.copy(errorMessage = null, showErrorModal = false) }
        }
    }

    // 기본 정보 입력
    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
        updateValidations()
    }

    fun onBirthClick() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun onSetBirth(birth: String) {
        val normalized = birth.ifBlank { null }
        _uiState.update { it.copy(birth = normalized, showDatePicker = false) }
        updateValidations()
    }
    fun onDismissDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    // 전화번호 & 인증
    fun onPhoneChange(newPhone: String) {
        // 하이픈 제거 후 숫자만 유지
        val digits = newPhone.filter { it.isDigit() }
        _uiState.update { it.copy(phoneNumber = digits) }
        updateValidations()
    }

    fun onRequestVerificationCode() {
        viewModelScope.launch {
            try {
                cancelVerificationTimer()

                val phoneNumber = _uiState.value.phoneNumber
                if (phoneNumber.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "전화번호를 입력해주세요.") }
                    return@launch
                }

                // API 호출
                userRepository.sendVerificationCode(phoneNumber).getOrThrow()

                val startSeconds = 180 // 3분
                _uiState.update {
                    it.copy(
                        isCodeRequested = true,
                        showTimerText = true,
                        remainingTimeText = formatSeconds(startSeconds),
                        verificationStatusMessage = "",
                        isCodeVerified = false,
                        remainingSeconds = startSeconds,
                        isTimerRunning = true
                    )
                }
                startVerificationTimer()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = "인증번호 발송에 실패했습니다: ${e.message}",
                        showErrorModal = true
                    )
                }
            }
        }
    }

    fun onVerificationCodeChange(newCode: String) {
        _uiState.update { it.copy(verificationCode = newCode) }
        updateValidations()
    }

    fun onVerifyTokenClick() {
        viewModelScope.launch {
            try {
                val phoneNumber = _uiState.value.phoneNumber
                val code = _uiState.value.verificationCode

                if (phoneNumber.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "전화번호를 입력해주세요.") }
                    return@launch
                }

                if (code.isBlank()) {
                    _uiState.update { it.copy(errorMessage = "인증번호를 입력해주세요.") }
                    return@launch
                }

                // API 호출 - verificationToken을 받아옴
                val verificationToken = userRepository.verifyCode(phoneNumber, code).getOrThrow()

                cancelVerificationTimer()
                _uiState.update {
                    it.copy(
                        isCodeVerified = true,
                        showTimerText = false,
                        verificationStatusMessage = "인증번호 등록에 성공했습니다.",
                        isTimerRunning = false,
                        remainingSeconds = 0,
                        remainingTimeText = "",
                        verificationToken = verificationToken  // 서버에서 받은 토큰 저장 (화면에 표시 안함)
                    )
                }
                updateValidations()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCodeVerified = false,
                        verificationStatusMessage = "올바른 인증번호가 아닙니다."
                    )
                }
                updateValidations()
            }
        }
    }

    private fun startVerificationTimer() {
        countdownJob = viewModelScope.launch {
            while (true) {
                val sec = _uiState.value.remainingSeconds
                if (sec <= 0) break
                delay(1_000)
                val next = sec - 1
                _uiState.update {
                    it.copy(
                        remainingSeconds = next,
                        remainingTimeText = formatSeconds(next),
                        isTimerRunning = next > 0
                    )
                }
            }
            // 시간 만료
            _uiState.update {
                it.copy(
                    showTimerText = false,
                    isCodeVerified = false,
                    verificationStatusMessage = "인증 시간이 만료되었습니다.",
                    isTimerRunning = false,
                    remainingSeconds = 0
                )
            }
        }
    }

    private fun cancelVerificationTimer() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.update {
            it.copy(
                isTimerRunning = false
            )
        }
    }

    private fun formatSeconds(total: Int): String {
        val m = total / 60
        val s = total % 60
        return String.format("%02d:%02d", m, s)
    }

    // ---------------------------
    // 비밀번호
    // ---------------------------
    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword) }
        updateValidations()
    }

    fun onPasswordConfirmChange(newPassword: String) {
        _uiState.update { it.copy(passwordConfirm = newPassword) }
        updateValidations()
    }

    fun onTogglePasswordVisible() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onTogglePasswordConfirmVisible() {
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    // ---------------------------
    // 프로필 이미지
    // ---------------------------
    fun onProfileImageClick() {
        // 갤러리/카메라 선택은 화면에서 처리
    }

    fun setProfileImage(path: String?) {
        _uiState.update { it.copy(profileImagePath = path) }
    }

    // ---------------------------
    // 회원 유형
    // ---------------------------
    fun onSelectGuardian() {
        _uiState.update { it.copy(userType = UserType.GUARDIAN) }
        updateValidations()
    }

    fun onSelectPatient() {
        _uiState.update { it.copy(userType = UserType.PATIENT) }
        updateValidations()
    }

    private fun setUserType(type: UserType) {
        _uiState.update { it.copy(userType = type) }
        updateValidations()
    }

    // ---------------------------
    // 회원가입
    // ---------------------------
    fun onSubmitRegister() {
        viewModelScope.launch {
            updateValidations()
            val state = _uiState.value
            if (!state.canSubmit) {
                _uiState.update { it.copy(showErrorModal = true) }
                return@launch
            }

            _uiState.update { it.copy(isSubmitting = true) }
            val result = performRegisterUser()

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showSuccessModal = true,
                            showErrorModal = false,
                            errorMessage = null,
                            registerSuccess = true
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            showSuccessModal = false,
                            showErrorModal = true,
                            errorMessage = error.message ?: "회원가입에 실패했습니다.",
                            registerSuccess = false
                        )
                    }
                }
            )
        }
    }

    private suspend fun performRegisterUser(): Result<Unit> {
        val s = _uiState.value
        return try {
            registerUseCase(
                name = s.name,
                birth = s.birth,
                phoneNumber = s.phoneNumber,
                verificationToken = s.verificationToken,
                password = s.password,
                userType = s.userType?.name ?: return Result.failure(Exception("회원 유형을 선택해주세요.")),
                profileImagePath = s.profileImagePath
            )
        } catch (t: Throwable) {
            val msg = mapError(t)
            Result.failure(Exception(msg))
        }
    }

    // ---------------------------
    // 검증 로직 (클라 사전 검증)
    // ---------------------------
    private fun updateValidations() {
        val s = _uiState.value

        val nameOk = s.name.isNotBlank()
        val birthOk = s.birth?.let { it.length == 8 && it.all(Char::isDigit) } ?: true
        val phoneOk = s.phoneNumber.isNotBlank() && s.phoneNumber.length in 10..11 && s.phoneNumber.all(Char::isDigit)
        val tokenOk = s.verificationToken.isNotBlank()
        val pwdOk = s.password.length in 8..16
        val pwdConfirmOk = s.passwordConfirm == s.password && s.passwordConfirm.isNotBlank()
        val userTypeOk = s.userType != null

        val canSubmit = nameOk && birthOk && phoneOk && tokenOk && pwdOk && pwdConfirmOk && userTypeOk

        _uiState.update {
            it.copy(
                isNameValid = nameOk,
                isBirthValid = birthOk,
                isPhoneValid = phoneOk,
                isVerificationValid = tokenOk,
                isPasswordValid = pwdOk,
                isPasswordConfirmValid = pwdConfirmOk,
                isUserTypeValid = userTypeOk,
                canSubmit = canSubmit
            )
        }
    }

    // ---------------------------
    // 모달 처리
    // ---------------------------
    fun onDismissSuccessModal() {
        _uiState.update { it.copy(showSuccessModal = false) }
        // 성공 후 로그인 화면 이동은 UI에서 처리
    }

    fun onDismissErrorModal() {
        _uiState.update { it.copy(showErrorModal = false) }
    }

    private fun mapError(t: Throwable): String {
        return when {
            t is HttpException && t.code() == 400 -> "이미 가입된 회원입니다."
            t is HttpException && t.code() == 422 -> "입력값이 유효하지 않습니다. 형식을 다시 확인해주세요."
            t is HttpException && (t.code() == 401 || t.code() == 403) -> "인증번호가 유효하지 않거나 만료되었습니다."
            t is HttpException && t.code() >= 500 -> "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            else -> "회원가입 중 오류가 발생했습니다. 네트워크 상태를 확인 후 다시 시도해주세요."
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelVerificationTimer()
    }
}