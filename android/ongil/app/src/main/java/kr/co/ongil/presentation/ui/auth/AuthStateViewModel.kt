package kr.co.ongil.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kr.co.ongil.data.datasource.local.preferences.TokenManager
import javax.inject.Inject

/**
 * 앱 시작 시 인증 상태를 확인하는 ViewModel
 */
@HiltViewModel
class AuthStateViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    init {
        checkLoginState()
    }

    /**
     * 저장된 토큰 확인하여 로그인 상태 체크
     */
    private fun checkLoginState() {
        viewModelScope.launch {
            val accessToken = tokenManager.getAccessToken().firstOrNull()
            _isLoggedIn.value = !accessToken.isNullOrEmpty()
        }
    }
}