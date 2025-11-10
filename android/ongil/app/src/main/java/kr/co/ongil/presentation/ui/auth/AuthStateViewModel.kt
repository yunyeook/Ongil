package kr.co.ongil.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flow
import kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
import kr.co.ongil.data.model.user.UserDto
import kr.co.ongil.domain.repository.UserRepository
import kr.co.ongil.domain.repository.FavoriteRepository
import kr.co.ongil.presentation.ui.favorite.PatientData
import javax.inject.Inject

/**
 * 앱 시작 시 인증 상태를 확인하는 ViewModel
 */
@HiltViewModel
class AuthStateViewModel @Inject constructor(
    private val userDataStoreManager: UserDataStoreManager,
    private val userRepository: UserRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

//    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
//    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn.asStateFlow()
//
//    private val _currentUserId = MutableStateFlow<Int?>(null)
//    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()
//
//    init {
//        checkLoginState()
//    }

    val currentUserId: StateFlow<String?> = userDataStoreManager.getLoginUserId().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000), null)
    val isLoggedIn: StateFlow<Boolean?> = currentUserId.map { it != null }.stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000), null)

//    /**
//     * 저장된 토큰 확인하여 로그인 상태 체크
//     */
//    private fun checkLoginState() {
//        viewModelScope.launch {
//            val accessToken = tokenManager.getAccessToken().firstOrNull()
//            val loggedIn = !accessToken.isNullOrEmpty()
//            _isLoggedIn.value = loggedIn
//
//            if (loggedIn) {
//                loadUserInfo()
//            }
//        }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUserInfo: Flow<Result<UserDto>?> = currentUserId.flatMapLatest { userId ->
        if (userId != null) {
            userRepository.getMyInfo()
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val patientList: StateFlow<List<PatientData>> = currentUserId.flatMapLatest { userId ->
        if (userId != null) {
            flow {
                val result = favoriteRepository.getMyRelationships()
                emit(result.getOrNull() ?: emptyList())
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedPatientId: StateFlow<String?> = userDataStoreManager.getSelectedPatientId()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * 환자 선택 시 DataStore에 저장
     */
    fun selectPatient(patientId: String) {
        viewModelScope.launch {
            userDataStoreManager.saveSelectedPatientId(patientId)
        }
    }

//    /**
//     * 현재 로그인한 사용자 정보 로드
//     */
//    private fun loadUserInfo() {
//        viewModelScope.launch {
//            userRepository.getMyInfo()
//                .onSuccess { userDto ->
//                    _currentUserId.value = userDto.id
//                    Log.d("AuthStateViewModel", "사용자 정보 로드 성공: userId=${userDto.id}, name=${userDto.name}, userType=${userDto.userType}")
//                }
//                .onFailure { error ->
//                    Log.e("AuthStateViewModel", "사용자 정보 로드 실패", error)
//                    _currentUserId.value = null
//                }
//        }
//    }
}