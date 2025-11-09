package kr.co.ongil.presentation.ui.searchuser

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.domain.repository.SearchUserRepository
import javax.inject.Inject

@HiltViewModel
class SearchUserViewModel @Inject constructor(
    private val repository: SearchUserRepository
) : ViewModel() {

    private fun normalizePhoneNumber(raw: String): String {
        return raw.filter { it.isDigit() }
    }

    // ---------- UI State ----------
    private val _uiState = MutableStateFlow(SearchUserUiState())
    val uiState: StateFlow<SearchUserUiState> = _uiState.asStateFlow()

    // ---------- Side Effect (단발 이벤트: Toast, Navigation 등) ----------
    private val _sideEffect = Channel<SearchUserUiSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    // ---------- Timer for verification countdown ----------
    private var countdownJob: Job? = null

    // 외부에서 ViewModel에 명령을 전달할 단일 엔트리
    fun onEvent(event: SearchUserUiEvent) {
        when (event) {
            is SearchUserUiEvent.OnPhoneInputChange -> handlePhoneInputChange(event.value)
            SearchUserUiEvent.OnClickSearch -> handleClickSearch()
            SearchUserUiEvent.OnClickRetrySearch -> handleClickRetrySearch()
            SearchUserUiEvent.OnClickAddFriend -> handleClickAddFriend()
            SearchUserUiEvent.OnClickBackToSearch -> handleBackToSearch()

            is SearchUserUiEvent.OnRelationshipNameChange -> handleRelationshipNameChange(event.value)
            is SearchUserUiEvent.OnRelationshipTypeSelect -> handleRelationshipTypeSelect(event.value)
            SearchUserUiEvent.OnClickRequestVerification -> handleClickRequestVerification()
            is SearchUserUiEvent.OnVerificationCodeChange -> handleVerificationCodeChange(event.value)
            SearchUserUiEvent.OnClickVerifyCode -> handleClickVerifyCode()
            is SearchUserUiEvent.OnMemoChange -> handleMemoChange(event.value)

            SearchUserUiEvent.OnClickSubmitRegister -> handleClickSubmitRegister()

            SearchUserUiEvent.OnClickGoToRegisterUser -> handleClickGoToRegisterUser()
            SearchUserUiEvent.OnClickSearchMore -> handleClickSearchMore()
        }
    }

    // ---------------------------------------------------------------------------------
    // 검색 단계 로직
    // ---------------------------------------------------------------------------------

    private fun handlePhoneInputChange(raw: String) {
        // TODO: "01012345678" -> "010-1234-5678" 같은 포맷팅 적용 가능
        _uiState.update {
            it.copy(
                phoneInput = raw,
                searchErrorMessage = null
            )
        }
    }

    private fun handleClickSearch() {
        val phone = _uiState.value.phoneInput
        if (phone.isBlank()) return

        _uiState.update {
            it.copy(
                isSearching = true,
                searchErrorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val normalizedPhone = normalizePhoneNumber(phone)
                // 실제 구현부: repository.searchUserByPhone
                // 성공 시 UserSummary?, 실패 시 null 리턴 가정
                val result = repository.searchUserByPhone(normalizedPhone)

                if (result != null) {
                    // 검색 성공
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            mode = SearchUserMode.SEARCH_SUCCESS,
                            foundUser = result,
                            // 등록 단계 기본값 세팅
                            relationshipName = result.displayName,
                            relationshipType = "",
                            relationshipTypeOptions = defaultRelationOptions(),
                            verificationCodeInput = "",
                            verificationToken = "",
                            verificationCountdownSec = DEFAULT_VERIFY_COUNTDOWN_SEC,
                            isVerificationValid = false,
                            memo = "",
                            isSubmitting = false,
                            submitErrorMessage = null
                        )
                    }
                } else {
                    // 검색 실패
                    _uiState.update {
                        it.copy(
                            isSearching = false,
                            mode = SearchUserMode.SEARCH_NOT_FOUND,
                            foundUser = null,
                            searchErrorMessage = "해당 번호로 등록된 사용자를 찾을 수 없습니다."
                        )
                    }
                }
            } catch (_: Throwable) {
                // 에러 발생 시에도 실패 화면으로 전환
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        mode = SearchUserMode.SEARCH_NOT_FOUND,
                        foundUser = null,
                        searchErrorMessage = "검색 중 오류가 발생했습니다."
                    )
                }
            }
        }
    }

    private fun handleClickRetrySearch() {
        // 검색 실패 화면에서 "다시 시도" 눌렀을 때
        resetToSearchInput()
    }

    private fun handleBackToSearch() {
        // 검색 성공 화면에서 "다시 검색하기" 눌렀을 때
        resetToSearchInput()
    }

    private fun resetToSearchInput() {
        _uiState.update {
            it.copy(
                mode = SearchUserMode.SEARCH_INPUT,
                isSearching = false,
                searchErrorMessage = null,
                foundUser = null,
                relationshipName = "",
                relationshipType = "",
                relationshipTypeOptions = emptyList(),
                verificationCodeInput = "",
                verificationToken = "",
                verificationCountdownSec = 0,
                isVerificationValid = false,
                memo = "",
                isSubmitting = false,
                submitErrorMessage = null
            )
        }
    }

    // ---------------------------------------------------------------------------------
    // 등록 단계 진입/입력 로직
    // ---------------------------------------------------------------------------------

    private fun handleClickAddFriend() {
        // 검색 성공 화면에서 "친구 추가하기" 눌렀을 때
        val found = _uiState.value.foundUser ?: return

        // 기존 타이머 취소
        cancelVerificationTimer()

        _uiState.update {
            it.copy(
                mode = SearchUserMode.REGISTER,
                relationshipName = found.displayName,
                relationshipTypeOptions = defaultRelationOptions(),
                isCodeRequested = false,
                verificationCountdownSec = 0,
                isVerificationValid = false,
                verificationCodeInput = "",
                verificationToken = "",
                submitErrorMessage = null
            )
        }
    }

    private fun handleRelationshipNameChange(value: String) {
        _uiState.update { it.copy(relationshipName = value.trim()) }
    }

    private fun handleRelationshipTypeSelect(value: String) {
        _uiState.update { it.copy(relationshipType = value) }
    }

    private fun handleClickRequestVerification() {
        // 인증번호 전송 버튼 눌렀을 때
        val found = _uiState.value.foundUser ?: return
        val targetPhone = found.phoneNumber

        // 기존 타이머 취소
        cancelVerificationTimer()

        viewModelScope.launch {
            // 인증번호 전송 요청
            val sent = try {
                repository.requestVerificationCode(
                    phoneNumber = normalizePhoneNumber(targetPhone)
                )
            } catch (_: Throwable) {
                false
            }

            if (!sent) {
                _sideEffect.send(
                    SearchUserUiSideEffect.ShowToast("인증번호 전송에 실패했습니다.")
                )
            } else {
                // 인증번호 전송 성공
                _uiState.update {
                    it.copy(
                        isCodeRequested = true,
                        verificationCountdownSec = DEFAULT_VERIFY_COUNTDOWN_SEC,
                        isVerificationValid = false,
                        verificationCodeInput = "",
                        verificationToken = ""
                    )
                }
                _sideEffect.send(
                    SearchUserUiSideEffect.ShowToast("인증번호를 전송했습니다.")
                )
                // 인증번호 전송 성공 시 타이머 시작
                startVerificationTimer()
            }
        }
    }

    private fun handleVerificationCodeChange(value: String) {
        _uiState.update {
            it.copy(
                verificationCodeInput = value,
                // 코드 바꾸면 다시 미검증 상태로
                isVerificationValid = false
            )
        }
    }

    private fun handleClickVerifyCode() {
        val state = _uiState.value
        val code = state.verificationCodeInput
        val phone = state.foundUser?.phoneNumber ?: return

        // 기본적인 입력 검증 (6자리)
        if (code.length != 6) {
            viewModelScope.launch {
                _sideEffect.send(
                    SearchUserUiSideEffect.ShowToast("6자리 인증번호를 입력하세요.")
                )
            }
            return
        }

        viewModelScope.launch {
            val (verified, token) = try {
                Log.d("SearchUserVM", "인증번호 검증 시작 - phoneNumber=$phone, code=$code, grants=RELATIONSHIP")
                repository.verifyCode(
                    phoneNumber = normalizePhoneNumber(phone),
                    verificationCode = code,
                    grants = "RELATIONSHIP"
                )
            } catch (e: Throwable) {
                Log.e("SearchUserVM", "인증번호 검증 예외 발생", e)
                _sideEffect.send(
                    SearchUserUiSideEffect.ShowToast("네트워크 오류가 발생했습니다. 다시 시도해주세요.")
                )
                false to null
            }

            Log.d("SearchUserVM", "인증번호 검증 결과 - verified=$verified, token=$token")

            if (verified && !token.isNullOrBlank()) {
                // 인증 성공 시 타이머 중단
                cancelVerificationTimer()

                _uiState.update {
                    it.copy(
                        isVerificationValid = true,
                        verificationToken = token,
                        verificationCountdownSec = 0
                    )
                }
                _sideEffect.send(
                    SearchUserUiSideEffect.ShowToast("인증이 완료되었습니다.")
                )
            } else {
                _uiState.update {
                    it.copy(
                        isVerificationValid = false,
                        verificationToken = ""
                    )
                }
                _sideEffect.send(
                    SearchUserUiSideEffect.ShowToast("인증번호가 올바르지 않습니다.")
                )
            }
        }
    }

    private fun handleMemoChange(value: String) {
        _uiState.update { state ->
            state.copy(
                memo = value.take(state.memoMaxLen)
            )
        }
    }

    // ---------------------------------------------------------------------------------
    // 등록 제출 / 완료 후 후처리
    // ---------------------------------------------------------------------------------

    private fun handleClickSubmitRegister() {
        val state = _uiState.value
        if (state.isSubmitting) return
        if (!state.canSubmitRegister) return

        _uiState.update { it.copy(isSubmitting = true, submitErrorMessage = null) }

        viewModelScope.launch {
            val success = try {
                repository.registerContact(
                    verificationToken = state.verificationToken,
                    relationshipName = state.relationshipName,
                    relationshipType = state.relationshipType
                )
            } catch (e: Throwable) {
                Log.e("SearchUserVM", "등록 실패", e)
                false
            }

            if (success) {
                // 성공 시 REGISTER_DONE 모드로 (완료 모달 띄우는 상태)
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        mode = SearchUserMode.REGISTER_DONE
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submitErrorMessage = "등록에 실패했습니다. 다시 시도해주세요."
                    )
                }

                _sideEffect.send(
                    SearchUserUiSideEffect.ShowToast("등록에 실패했습니다.")
                )
            }
        }
    }

    private fun handleClickGoToRegisterUser() {
        // 완료 모달에서 "연락처 보기" / "추가된 사용자 보기" 등
        val registeredUserId = _uiState.value.foundUser?.id ?: return

        viewModelScope.launch {
            _sideEffect.send(
                SearchUserUiSideEffect.NavigateToRegisterUser(
                    userId = registeredUserId
                )
            )
        }
    }

    private fun handleClickSearchMore() {
        // 완료 모달에서 "더 검색하기"
        // 1) 상태를 초기 검색 단계로 돌리고
        // 2) UI에게 초기화(포커스 등) 신호를 줄 수도 있음
        _uiState.update {
            it.copy(
                mode = SearchUserMode.SEARCH_INPUT,
                isSearching = false,
                searchErrorMessage = null,
                foundUser = null,
                relationshipName = "",
                relationshipType = "",
                relationshipTypeOptions = emptyList(),
                verificationCodeInput = "",
                verificationToken = "",
                verificationCountdownSec = 0,
                isVerificationValid = false,
                memo = "",
                isSubmitting = false,
                submitErrorMessage = null
            )
        }

        viewModelScope.launch {
            _sideEffect.send(
                SearchUserUiSideEffect.BackToSearchStart
            )
        }
    }

    // ---------------------------------------------------------------------------------
    // 내부 헬퍼
    // ---------------------------------------------------------------------------------

    private fun defaultRelationOptions(): List<String> {
        return listOf("자녀", "부모", "배우자", "기타")
    }

    // ---------------------------------------------------------------------------------
    // 타이머 관련 (SignupViewModel과 동일)
    // ---------------------------------------------------------------------------------

    private fun startVerificationTimer() {
        countdownJob?.cancel() // 기존 타이머가 있으면 취소

        countdownJob = viewModelScope.launch {
            var remainingSeconds = _uiState.value.verificationCountdownSec

            while (remainingSeconds > 0) {
                delay(1_000)
                remainingSeconds -= 1
                _uiState.update {
                    it.copy(verificationCountdownSec = remainingSeconds)
                }
            }

            // 시간 만료 시
            _uiState.update {
                it.copy(
                    verificationCountdownSec = 0,
                    isVerificationValid = false
                )
            }

            _sideEffect.send(
                SearchUserUiSideEffect.ShowToast("인증 시간이 만료되었습니다.")
            )
        }
    }

    private fun cancelVerificationTimer() {
        countdownJob?.cancel()
        countdownJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelVerificationTimer()
    }

    companion object {
        private const val DEFAULT_VERIFY_COUNTDOWN_SEC = 180 // 예: 3분 타이머
    }
}