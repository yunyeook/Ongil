package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

/**
 * 아직 서버 없으니까 Fake 로직으로 성공/실패만 분기
 * - 비번이 "1234"면 성공, 아니면 실패
 */
class LoginViewModel : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    fun login(phone: String, password: String) {
        if (phone.isBlank()) {
            fail("전화번호를 입력해주세요.")
            return
        }
        if (password.isBlank()) {
            fail("비밀번호를 입력해주세요.")
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            delay(800) // 로딩 흉내
            if (password == "1234") {
                _ui.update { it.copy(isLoading = false, isLoggedIn = true) }
            } else {
                fail("전화번호 또는 비밀번호가 올바르지 않습니다.")
            }
        }
    }

    private fun fail(message: String) {
        _ui.update { it.copy(isLoading = false, error = message, isLoggedIn = false) }
    }

    fun consumeError() {
        _ui.update { it.copy(error = null) }
    }
}
