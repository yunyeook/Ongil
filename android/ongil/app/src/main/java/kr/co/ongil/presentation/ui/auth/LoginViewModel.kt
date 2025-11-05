// presentation/ui/auth/LoginViewModel.kt
package kr.co.ongil.presentation.ui.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.viewModelScope

sealed interface LoginEffect {
    object NavigateHome : LoginEffect  // 홈 화면으로 이동
    data class ShowSnack(val message: String) : LoginEffect  // 스낵바 메시지 표시
}

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

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

            // 임시 로그인 로직 (나중에 UseCase로 교체)
            if (s.password == "1234") {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(LoginEffect.NavigateHome)  // 로그인 성공 시 홈 화면으로 이동
            } else {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(LoginEffect.ShowSnack("전화번호 또는 비밀번호가 올바르지 않습니다."))
            }
        }
    }

    private fun LoginUiState.revalidate(): LoginUiState {
        val enabled = phone.length in 10..11 && password.length >= 4
        return copy(isLoginEnabled = enabled)
    }
}
