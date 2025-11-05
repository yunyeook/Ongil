package kr.co.ongil.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface LoginEffect {
    data object NavigateHome : LoginEffect
    data class ShowSnack(val message: String) : LoginEffect
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    // TODO: 명세서 나오면 LoginUseCase 주입
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v).revalidate() }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v).revalidate() }

    fun onClickLogin() {
        val s = _state.value
        if (s.isLoading) return
        if (!s.isLoginEnabled) {
            viewModelScope.launch { _effect.emit(LoginEffect.ShowSnack("전화번호/비밀번호를 확인해주세요.")) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // 임시 로그인 로직 (나중에 UseCase로 교체)
            delay(600)
            if (s.password == "1234") {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(LoginEffect.NavigateHome)
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
