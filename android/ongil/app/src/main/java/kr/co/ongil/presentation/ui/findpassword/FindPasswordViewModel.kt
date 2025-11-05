package kr.co.ongil.presentation.ui.findpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface FindPasswordAuthRepository {
    suspend fun sendVerificationCode(phone: String): Result<Unit>
    suspend fun verifyCode(phone: String, code: String): Result<Boolean>
}

@HiltViewModel
class FindPasswordViewModel @Inject constructor(
    private val authRepository: FindPasswordAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindPasswordUiState())
    val uiState: StateFlow<FindPasswordUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<FindPasswordUiSideEffect>()
    val effect: SharedFlow<FindPasswordUiSideEffect> = _effect.asSharedFlow()

    private var timerJob: Job? = null

    fun onEvent(event: FindPasswordUiEvent) {
        when (event) {
            is FindPasswordUiEvent.ChangePhone -> onChangePhone(event.input)
            is FindPasswordUiEvent.ChangeCode -> onChangeCode(event.input)
            FindPasswordUiEvent.ClickSendCode -> onClickSendCode()
            FindPasswordUiEvent.ClickResendCode -> onClickResendCode()
            FindPasswordUiEvent.ClickVerifyCode -> onClickVerifyCode()
            FindPasswordUiEvent.ClickResetPassword -> onClickResetPassword()
            FindPasswordUiEvent.ClickBack -> emitEffect(FindPasswordUiSideEffect.NavigateBack)
            FindPasswordUiEvent.ClickGoLogin -> emitEffect(FindPasswordUiSideEffect.NavigateLogin)
            FindPasswordUiEvent.ClickHelp -> emitEffect(FindPasswordUiSideEffect.ShowToast("고객센터로 연결합니다."))
        }
    }

    private fun onChangePhone(input: String) {
        val digits = input.filter { it.isDigit() }
        val valid = digits.length == 10 || digits.length == 11
        _uiState.update { it.copy(phone = input, isPhoneValid = valid) }
    }

    private fun onChangeCode(input: String) {
        _uiState.update { state ->
            val trimmed = input.take(state.codeLength).filter { it.isDigit() }
            state.copy(code = trimmed, codeVerifyStatus = if (state.codeVerifyStatus == CodeVerifyStatus.Success) CodeVerifyStatus.Success else CodeVerifyStatus.Idle)
        }
    }

    private fun onClickSendCode() {
        val state = _uiState.value
        if (!state.isPhoneValid) return
        viewModelScope.launch {
            authRepository.sendVerificationCode(state.phoneDigits()).onSuccess {
                _uiState.update {
                    it.copy(
                        isCodeRequested = true,
                        code = "",
                        remainingSec = THREE_MIN_SEC,
                        codeVerifyStatus = CodeVerifyStatus.Idle,
                        isResetEnabled = false
                    )
                }
                startTimer()
            }.onFailure {
                emitEffect(FindPasswordUiSideEffect.ShowToast("인증번호 발송에 실패했습니다."))
            }
        }
    }

    private fun onClickResendCode() {
        val phone = _uiState.value.phoneDigits()
        viewModelScope.launch {
            authRepository.sendVerificationCode(phone).onSuccess {
                _uiState.update {
                    it.copy(
                        isCodeRequested = true,
                        code = "",
                        remainingSec = THREE_MIN_SEC,
                        codeVerifyStatus = CodeVerifyStatus.Idle,
                        isResetEnabled = false
                    )
                }
                startTimer()
            }.onFailure {
                emitEffect(FindPasswordUiSideEffect.ShowToast("재전송에 실패했습니다."))
            }
        }
    }

    private fun onClickVerifyCode() {
        val state = _uiState.value
        if (state.remainingSec <= 0) {
            _uiState.update { it.copy(codeVerifyStatus = CodeVerifyStatus.Expired, isResetEnabled = false) }
            return
        }
        if (state.code.length != state.codeLength) return
        viewModelScope.launch {
            authRepository.verifyCode(state.phoneDigits(), state.code).onSuccess { ok ->
                if (ok) {
                    _uiState.update { it.copy(codeVerifyStatus = CodeVerifyStatus.Success, isResetEnabled = true) }
                } else {
                    _uiState.update { it.copy(codeVerifyStatus = CodeVerifyStatus.Error, isResetEnabled = false) }
                }
            }.onFailure {
                _uiState.update { it.copy(codeVerifyStatus = CodeVerifyStatus.Error, isResetEnabled = false) }
            }
        }
    }

    private fun onClickResetPassword() {
        if (_uiState.value.isResetEnabled) {
            emitEffect(FindPasswordUiSideEffect.NavigateResetPassword)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSec > 0) {
                delay(1000)
                _uiState.update { it.copy(remainingSec = it.remainingSec - 1) }
            }
            _uiState.update {
                if (it.codeVerifyStatus != CodeVerifyStatus.Success) it.copy(codeVerifyStatus = CodeVerifyStatus.Expired)
                else it
            }
        }
    }

    private fun emitEffect(effect: FindPasswordUiSideEffect) {
        viewModelScope.launch { _effect.emit(effect) }
    }

    private fun FindPasswordUiState.phoneDigits(): String = phone.filter { it.isDigit() }

    companion object {
        private const val THREE_MIN_SEC = 180
    }
}