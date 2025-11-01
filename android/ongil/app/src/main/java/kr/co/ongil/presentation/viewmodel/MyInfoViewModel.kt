package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.presentation.ui.myinfo.MyInfoUiState
import kr.co.ongil.core.utils.formatPhoneNumber

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
    // TODO: Repository 주입
    // private val userRepository: UserRepository
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

            try {
                // TODO: Repository에서 사용자 정보 가져오기
                // val userDto = userRepository.getMyInfo()

                // 임시 데이터 (실제로는 API에서 가져옴)
                val userDto = UserDto(
                    id = 1,
                    name = "홍길동",
                    birth = "19980919",
                    phoneNumber = "01012341234",
                    userType = "PATIENT",
                    profileImage = null
                )

                // DTO → UiState 변환
                _uiState.value = MyInfoUiState(
                    name = userDto.name,
                    phoneNumber = formatPhoneNumber(userDto.phoneNumber),
                    profileImage = userDto.profileImage,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "사용자 정보를 불러오는데 실패했습니다."
                )
            }
        }
    }

    /**
     * 로그아웃
     */
    fun logout() {
        viewModelScope.launch {
            // TODO: 로그아웃 처리
            // authRepository.logout()
        }
    }
}
