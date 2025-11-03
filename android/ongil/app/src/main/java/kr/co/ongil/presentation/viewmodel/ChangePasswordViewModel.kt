package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.core.utils.PasswordValidationResult
import kr.co.ongil.core.utils.validatePasswordChange
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

        // 유효성 검사 (ValidationUtils 사용)
        val validationResult = validatePasswordChange(
            currentPassword = currentState.currentPassword,
            newPassword = currentState.newPassword,
            confirmPassword = currentState.confirmPassword
        )

        when (validationResult) {
            is PasswordValidationResult.Invalid -> {
                _uiState.update { it.copy(error = validationResult.message) }
                return
            }
            is PasswordValidationResult.Valid -> {
                // 검증 통과, API 호출 진행
            }
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
}
