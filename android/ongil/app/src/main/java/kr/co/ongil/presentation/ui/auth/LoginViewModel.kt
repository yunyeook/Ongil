// presentation/ui/auth/LoginViewModel.kt
package kr.co.ongil.presentation.ui.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kr.co.ongil.domain.usecase.auth.LoginUseCase

sealed interface LoginEffect {
    object NavigateHome : LoginEffect  // 홈 화면으로 이동
    data class ShowSnack(val message: String) : LoginEffect  // 스낵바 메시지 표시
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect

    // 전화번호 입력 처리
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v).revalidate() }

    // 비밀번호 입력 처리
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v).revalidate() }

    // 로그인 버튼 클릭 처리
    fun onClickLogin() {
        val s = _state.value
        if (s.isLoading) return
        if (!s.isLoginEnabled) {
            viewModelScope.launch {
                _effect.emit(LoginEffect.ShowSnack("전화번호/비밀번호를 확인해주세요."))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }  // 로딩 상태로 업데이트

            // LoginUseCase를 통한 로그인 처리
            loginUseCase(phoneNumber = s.phone, password = s.password)
                .onSuccess { loginResponse ->
                    // 로그인 성공
                    // TODO: 토큰 저장 처리 (TokenManager에서 처리하도록 변경 필요)
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(LoginEffect.NavigateHome)  // 홈 화면으로 이동
                }
                .onFailure { exception ->
                    // 로그인 실패
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(LoginEffect.ShowSnack(exception.message ?: "로그인에 실패했습니다."))
                }
        }
    }

    private fun LoginUiState.revalidate(): LoginUiState {
        val enabled = phone.length in 10..11 && password.length >= 4
        return copy(isLoginEnabled = enabled)
    }
}
