package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.domain.repository.UserRepository
import kr.co.ongil.presentation.uistate.ChangePasswordEvent
import kr.co.ongil.presentation.uistate.ChangePasswordUiState

/**
 * 비밀번호 변경 화면 ViewModel
 */
class ChangePasswordViewModel(
    private val userRepository: UserRepository? = null
    // TODO: DI(Hilt/Koin)로 주입하도록 변경
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    /**
     * 이벤트 처리
     */
    fun onEvent(event: ChangePasswordEvent) {
        when (event) {
            is ChangePasswordEvent.UpdateCurrentPassword -> {
                _uiState.update { it.copy(currentPassword = event.password) }
            }

            is ChangePasswordEvent.UpdateNewPassword -> {
                _uiState.update { it.copy(newPassword = event.password) }
            }

            is ChangePasswordEvent.UpdateConfirmPassword -> {
                _uiState.update { it.copy(confirmPassword = event.password) }
            }

            is ChangePasswordEvent.ToggleCurrentPasswordVisibility -> {
                _uiState.update { it.copy(currentPasswordVisible = !it.currentPasswordVisible) }
            }

            is ChangePasswordEvent.ToggleNewPasswordVisibility -> {
                _uiState.update { it.copy(newPasswordVisible = !it.newPasswordVisible) }
            }

            is ChangePasswordEvent.ToggleConfirmPasswordVisibility -> {
                _uiState.update { it.copy(confirmPasswordVisible = !it.confirmPasswordVisible) }
            }

            is ChangePasswordEvent.ChangePassword -> {
                changePassword()
            }
        }
    }

    /**
     * 비밀번호 변경
     */
    private fun changePassword() {
        val currentState = _uiState.value

        // 유효성 검사
        if (!validatePassword(currentState)) {
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // API 호출
                userRepository?.changePassword(
                    currentPassword = currentState.currentPassword,
                    newPassword = currentState.newPassword
                )?.getOrThrow()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "비밀번호 변경에 실패했습니다."
                    )
                }
            }
        }
    }

    /**
     * 비밀번호 유효성 검사
     */
    private fun validatePassword(state: ChangePasswordUiState): Boolean {
        return when {
            state.currentPassword.isBlank() -> {
                _uiState.update { it.copy(error = "현재 비밀번호를 입력해주세요.") }
                false
            }
            state.newPassword.isBlank() -> {
                _uiState.update { it.copy(error = "새 비밀번호를 입력해주세요.") }
                false
            }
            state.newPassword.length < 8 -> {
                _uiState.update { it.copy(error = "비밀번호는 8자 이상이어야 합니다.") }
                false
            }
            !isValidPasswordFormat(state.newPassword) -> {
                _uiState.update { it.copy(error = "영문, 숫자, 특수문자를 포함해야 합니다.") }
                false
            }
            state.newPassword != state.confirmPassword -> {
                _uiState.update { it.copy(error = "비밀번호가 일치하지 않습니다.") }
                false
            }
            state.currentPassword == state.newPassword -> {
                _uiState.update { it.copy(error = "현재 비밀번호와 새 비밀번호가 같습니다.") }
                false
            }
            else -> true
        }
    }

    /**
     * 비밀번호 형식 검사 (영문, 숫자, 특수문자 포함)
     */
    private fun isValidPasswordFormat(password: String): Boolean {
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasLetter && hasDigit && hasSpecialChar
    }
}
