package kr.co.ongil.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kr.co.ongil.core.utils.formatPhoneNumber
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.domain.repository.AuthRepository
import kr.co.ongil.domain.usecase.user.GetUserUseCase
import kr.co.ongil.presentation.ui.myinfo.MyInfoUiState
import javax.inject.Inject

/**
 * 나의 정보 화면 ViewModel
 *
 * 사용 예시:
 * ```
 * val viewModel: MyInfoViewModel = viewModel()
 * val uiState by viewModel.uiState.collectAsState()
 *
 * MyInfoScreen(
 *     uiState = uiState,
 *     onEditInfo = { ... },
 *     onRecentCalls = { ... },
 *     onLogout = { viewModel.logout() }
 * )
 * ```
 */
@HiltViewModel
class MyInfoViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val authRepository: AuthRepository,
    private val userDataStoreManager: UserDataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyInfoUiState())
    val uiState: StateFlow<MyInfoUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<MyInfoSideEffect>()
    val sideEffect: SharedFlow<MyInfoSideEffect> = _sideEffect.asSharedFlow()

    init {
        loadUserInfo()
    }

    /**
     * 사용자 정보 로드
     */
    fun loadUserInfo() {
        Log.d(TAG, "loadUserInfo() called")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            Log.d(TAG, "Starting getUserUseCase call")

            // DataStore에서 userId 가져오기
            val userId = userDataStoreManager.getLoginUserId().first()

            // DataStore에서 캐시된 프로필 이미지 먼저 로드
            if (userId != null) {
                val cachedProfileImage = userDataStoreManager.getProfileImage(userId).first()
                if (cachedProfileImage != null) {
                    _uiState.update { it.copy(profileImage = cachedProfileImage) }
                    Log.d(TAG, "Loaded cached profile image from DataStore")
                }
            }

            // GetUserUseCase를 통한 사용자 정보 조회 (API)
            getUserUseCase()
                .onEach { result ->
                    result.onSuccess { userDto ->
                        // DTO → UiState 변환
                        Log.d(TAG, "getUserUseCase success: $userDto")
                        _uiState.value = MyInfoUiState(
                            name = userDto.name,
                            phoneNumber = formatPhoneNumber(userDto.phoneNumber),
                            profileImage = userDto.profileImage,
                            isLoading = false
                        )
                        Log.d(TAG, "UiState updated: ${_uiState.value}")

                        // API 응답의 프로필 이미지를 DataStore에 저장
                        if (userId != null && userDto.profileImage != null) {
                            userDataStoreManager.saveProfileImage(userId, userDto.profileImage)
                        }
                    }.onFailure { exception ->
                        Log.e(TAG, "getUserUseCase failed", exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = exception.message ?: "사용자 정보를 불러오는데 실패했습니다."
                        )
                    }
                }
                .collect()
        }
    }

    /**
     * 로그아웃
     */
    fun logout() {
        Log.d(TAG, "logout() called")
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            Log.d(TAG, "Starting logout API call")

            authRepository.logout()
                .onSuccess { message ->
                    // 로그아웃 성공 - 로그인 화면으로 이동
                    Log.d(TAG, "Logout success: $message")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _sideEffect.emit(MyInfoSideEffect.NavigateToLogin)
                    Log.d(TAG, "NavigateToLogin emitted")
                }
                .onFailure { exception ->
                    Log.e(TAG, "Logout failed", exception)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "로그아웃에 실패했습니다."
                    )
                }
        }
    }

    companion object {
        private const val TAG = "MyInfoViewModel"
    }
}

/**
 * MyInfo 화면 SideEffect
 */
sealed interface MyInfoSideEffect {
    /**
     * 로그인 화면으로 이동 (로그아웃 성공 시)
     */
    object NavigateToLogin : MyInfoSideEffect
}
