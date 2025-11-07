package kr.co.ongil.presentation.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kr.co.ongil.data.datasource.local.preferences.TokenManager
import kr.co.ongil.domain.repository.UserRepository
import javax.inject.Inject

/**
 * 앱 시작 시 인증 상태를 확인하는 ViewModel
 */
@HiltViewModel
class AuthStateViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    init {
        checkLoginState()
    }

    /**
     * 저장된 토큰 확인하여 로그인 상태 체크
     */
    private fun checkLoginState() {
        viewModelScope.launch {
            val accessToken = tokenManager.getAccessToken().firstOrNull()
            val loggedIn = !accessToken.isNullOrEmpty()
            _isLoggedIn.value = loggedIn

            if (loggedIn) {
                loadUserInfo()
            }
        }
    }

    /**
     * 현재 로그인한 사용자 정보 로드
     */
    private fun loadUserInfo() {
        viewModelScope.launch {
            userRepository.getMyInfo()
                .onSuccess { userDto ->
                    _currentUserId.value = userDto.id
                    Log.d("AuthStateViewModel", "사용자 정보 로드 성공: userId=${userDto.id}, name=${userDto.name}, userType=${userDto.userType}")
                }
                .onFailure { error ->
                    Log.e("AuthStateViewModel", "사용자 정보 로드 실패", error)
                    _currentUserId.value = null
                }
        }
    }
}