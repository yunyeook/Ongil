package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.co.ongil.core.utils.formatPhoneNumber
import kr.co.ongil.domain.repository.AuthRepository
import kr.co.ongil.domain.repository.UserRepository
import kr.co.ongil.presentation.ui.myinfo.MyInfoUiState

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
class MyInfoViewModel(
    private val userRepository: UserRepository = kr.co.ongil.data.repository.UserRepositoryImpl(),
    private val authRepository: AuthRepository = kr.co.ongil.data.repository.AuthRepositoryImpl()
    // TODO: DI(Hilt/Koin)로 주입하도록 변경
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyInfoUiState())
    val uiState: StateFlow<MyInfoUiState> = _uiState.asStateFlow()

    init {
        loadUserInfo()
    }

    /**
     * 사용자 정보 로드
     */
    fun loadUserInfo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            userRepository.getMyInfo()
                .onSuccess { userDto ->
                    // DTO → UiState 변환
                    _uiState.value = MyInfoUiState(
                        name = userDto.name,
                        phoneNumber = formatPhoneNumber(userDto.phoneNumber),
                        profileImage = userDto.profileImage,
                        isLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "사용자 정보를 불러오는데 실패했습니다."
                    )
                }
        }
    }

    /**
     * 로그아웃
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.logout()
                .onSuccess { message ->
                    // 로그아웃 성공
                    // Navigation은 UI에서 처리
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "로그아웃에 실패했습니다."
                    )
                }
        }
    }
}
