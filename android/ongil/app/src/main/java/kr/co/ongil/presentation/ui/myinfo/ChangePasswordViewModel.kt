package kr.co.ongil.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.core.utils.PasswordValidationResult
import kr.co.ongil.core.utils.validatePasswordChange
import kr.co.ongil.domain.repository.FindPasswordAuthRepository
import kr.co.ongil.domain.repository.UserRepository
import kr.co.ongil.presentation.uistate.ChangePasswordEvent
import kr.co.ongil.presentation.uistate.ChangePasswordUiState
import javax.inject.Inject

/**
 * 비밀번호 변경 화면 ViewModel
 */
@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val findPasswordAuthRepository: FindPasswordAuthRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ChangePasswordVM"
    }

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

            is ChangePasswordEvent.SetVerificationToken -> {
                _uiState.update { it.copy(verificationToken = event.token) }
            }

            is ChangePasswordEvent.ChangePassword -> {
                changePassword()
            }
        }
    }

    /**
     * 비밀번호 변경 또는 재설정
     */
    private fun changePassword() {
        val currentState = _uiState.value
        Log.d(TAG, "changePassword() called, verificationToken: ${currentState.verificationToken != null}")

        // verificationToken이 있으면 비밀번호 재설정, 없으면 비밀번호 변경
        if (currentState.verificationToken != null) {
            resetPassword(currentState.verificationToken)
        } else {
            changePasswordWithCurrent()
        }
    }

    /**
     * 비밀번호 변경 (현재 비밀번호 필요)
     */
    private fun changePasswordWithCurrent() {
        val currentState = _uiState.value
        Log.d(TAG, "changePasswordWithCurrent() called")

        // 유효성 검사 (ValidationUtils 사용)
        val validationResult = validatePasswordChange(
            currentPassword = currentState.currentPassword,
            newPassword = currentState.newPassword,
            confirmPassword = currentState.confirmPassword
        )

        when (validationResult) {
            is PasswordValidationResult.Invalid -> {
                Log.w(TAG, "Validation failed: ${validationResult.message}")
                _uiState.update { it.copy(error = validationResult.message) }
                return
            }
            is PasswordValidationResult.Valid -> {
                Log.d(TAG, "Validation passed, proceeding with API call")
            }
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                Log.d(TAG, "Calling changePassword API")

                // API 호출
                userRepository.changePassword(
                    currentPassword = currentState.currentPassword,
                    newPassword = currentState.newPassword
                ).getOrThrow()

                Log.d(TAG, "Password change successful")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Password change failed", e)
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
     * 비밀번호 재설정 (verificationToken 사용)
     */
    private fun resetPassword(verificationToken: String) {
        val currentState = _uiState.value
        Log.d(TAG, "resetPassword() called")

        // 비밀번호 일치 확인
        if (currentState.newPassword != currentState.confirmPassword) {
            _uiState.update { it.copy(error = "비밀번호가 일치하지 않습니다.") }
            return
        }

        // 비밀번호 길이 확인
        if (currentState.newPassword.length < 8) {
            _uiState.update { it.copy(error = "비밀번호는 8자 이상이어야 합니다.") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                Log.d(TAG, "Calling resetPassword API")

                // API 호출
                findPasswordAuthRepository.resetPassword(
                    verificationToken = verificationToken,
                    newPassword = currentState.newPassword,
                    confirmPassword = currentState.confirmPassword
                ).getOrThrow()

                Log.d(TAG, "Password reset successful")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Password reset failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "비밀번호 재설정에 실패했습니다."
                    )
                }
            }
        }
    }
}
