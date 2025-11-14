package kr.co.ongil.wear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kr.co.ongil.wear.data.datasource.sync.PhoneDataSyncManager  // ← 추가
import kr.co.ongil.wear.data.model.WearLoginData
import kr.co.ongil.wear.domain.usecase.SyncLoginDataUseCase
import javax.inject.Inject

/**
 * 워치 인증 ViewModel
 *
 * 스프링부트의 Controller와 비슷
 * - UI 상태 관리
 * - 사용자 액션 처리
 * - UseCase 호출
 */
@HiltViewModel  // Hilt가 ViewModel 자동 생성
class WearAuthViewModel @Inject constructor(
    private val syncLoginDataUseCase: SyncLoginDataUseCase,
    private val phoneDataSyncManager: PhoneDataSyncManager  // ← 추가
) : ViewModel() {

    // === UI 상태 관리 ===

    /**
     * 로그인 상태
     *
     * private MutableStateFlow = 내부에서만 수정 가능
     * public StateFlow = 외부에서는 읽기만 가능
     *
     * 스프링의 private 필드 + public getter와 비슷
     */
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * 사용자 ID
     */
    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    /**
     * 사용자 타입 (PATIENT or GUARDIAN)
     */
    private val _userType = MutableStateFlow<String?>(null)
    val userType: StateFlow<String?> = _userType.asStateFlow()

    /**
     * 로딩 상태
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * 에러 메시지
     */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // === 초기화 ===

    init {
        // ViewModel 생성 시 자동 실행
        observeLoginState()
        setupPhoneDataSync()
        fetchInitialData()
    }

    /**
     * 로그인 상태 관찰
     *
     * DataStore 변경 시 자동으로 UI 상태 업데이트
     */
    private fun observeLoginState() {

        viewModelScope.launch {
            // 로그인 상태 관찰
            syncLoginDataUseCase.isLoggedIn().collectLatest { loggedIn ->
                _isLoggedIn.value = loggedIn
            }
        }

        viewModelScope.launch {
            // 사용자 정보 관찰
            syncLoginDataUseCase.getUserInfo().collectLatest { (userId, userType) ->
                _userId.value = userId
                _userType.value = userType
            }
        }
    }

    /**
     * 폰 데이터 동기화 설정
     *
     * 폰에서 데이터 받으면 자동으로 저장
     */
    private fun setupPhoneDataSync() {
        // 리스너 설정
        phoneDataSyncManager.setOnLoginDataReceivedListener { loginData ->
            viewModelScope.launch {
                syncLoginData(loginData)
            }
        }

        // 리스닝 시작
        phoneDataSyncManager.startListening()
    }

    /**
     * 초기 데이터 가져오기
     *
     * 앱 시작 시 폰에서 이미 보낸 데이터 확인
     */
    private fun fetchInitialData() {
        viewModelScope.launch {
            try {
                val loginData = phoneDataSyncManager.fetchCurrentLoginData()
                if (loginData != null && loginData.accessToken.isNotEmpty()) {
                    syncLoginData(loginData)
                }
            } catch (e: Exception) {
                // 에러 무시 (폰에서 아직 안 보냈을 수 있음)
            }
        }
    }

    /**
     * ViewModel 파괴 시 리스너 중지
     */
    override fun onCleared() {
        super.onCleared()
        phoneDataSyncManager.stopListening()
    }

    // === 사용자 액션 처리 ===

    /**
     * 로그인 정보 동기화
     *
     * 폰에서 받은 로그인 정보를 워치에 저장
     *
     * @param loginData 폰에서 받은 로그인 정보
     */
    fun syncLoginData(loginData: WearLoginData) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                // UseCase 실행
                syncLoginDataUseCase(loginData)

                // 성공
                _isLoading.value = false
            } catch (e: Exception) {
                // 에러 처리
                _errorMessage.value = e.message ?: "로그인 동기화 실패"
                _isLoading.value = false
            }
        }
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
