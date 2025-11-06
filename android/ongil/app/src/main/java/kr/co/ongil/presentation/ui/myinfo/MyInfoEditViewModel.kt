package kr.co.ongil.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.domain.repository.UserRepository
import kr.co.ongil.domain.usecase.user.GetUserUseCase
import kr.co.ongil.domain.usecase.user.UpdateUserUseCase
import kr.co.ongil.presentation.uistate.MyInfoEditEvent
import kr.co.ongil.presentation.uistate.MyInfoEditUiState
import kr.co.ongil.presentation.uistate.PhoneUiState
import java.io.File
import javax.inject.Inject

/**
 * 내 정보 수정 화면 ViewModel
 */
@HiltViewModel
class MyInfoEditViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "MyInfoEditVM"
    }

    private val _uiState = MutableStateFlow(MyInfoEditUiState())
    val uiState: StateFlow<MyInfoEditUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadUserInfo()
    }

    /**
     * 사용자 정보 로드
     */
    private fun loadUserInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getUserUseCase()
                .onSuccess { userDto ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            name = userDto.name,
                            birth = userDto.birth,
                            phone = kr.co.ongil.core.utils.formatPhoneNumber(userDto.phoneNumber),
                            profileImageUrl = userDto.profileImage,
                            roleLabel = if (userDto.userType == "PATIENT") "환자" else "보호자",
                            isLoading = false
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "사용자 정보를 불러오는데 실패했습니다."
                        )
                    }
                }
        }
    }

    /**
     * 이벤트 처리
     */
    fun onEvent(event: MyInfoEditEvent) {
        when (event) {
            is MyInfoEditEvent.UpdateName -> {
                _uiState.update { it.copy(name = event.name) }
            }

            is MyInfoEditEvent.UpdateBirth -> {
                _uiState.update { it.copy(birth = event.birth) }
            }

            is MyInfoEditEvent.PickProfileImage -> {
                _uiState.update { it.copy(showImagePicker = true) }
            }

            is MyInfoEditEvent.OnImageSelected -> {
                _uiState.update {
                    it.copy(
                        selectedImageUri = event.uri,
                        profileImageUrl = event.uri,  // 미리보기용
                        showImagePicker = false
                    )
                }
            }

            is MyInfoEditEvent.DismissImagePicker -> {
                _uiState.update { it.copy(showImagePicker = false) }
            }

            is MyInfoEditEvent.PickBirthDate -> {
                _uiState.update { it.copy(showDatePicker = true) }
            }

            is MyInfoEditEvent.OnDateSelected -> {
                _uiState.update {
                    it.copy(
                        birth = event.date,
                        showDatePicker = false
                    )
                }
            }

            is MyInfoEditEvent.DismissDatePicker -> {
                _uiState.update { it.copy(showDatePicker = false) }
            }

            is MyInfoEditEvent.StartPhoneEdit -> {
                _uiState.update {
                    it.copy(
                        phoneUiState = PhoneUiState.Editing,
                        newPhone = "",
                        verificationCode = "",
                        verificationResult = null
                    )
                }
            }

            is MyInfoEditEvent.UpdateNewPhone -> {
                _uiState.update { it.copy(newPhone = event.phone) }
            }

            is MyInfoEditEvent.SendVerificationCode -> {
                sendVerificationCode(event.phone)
            }

            is MyInfoEditEvent.ResendVerificationCode -> {
                resendVerificationCode(event.phone)
            }

            is MyInfoEditEvent.UpdateVerificationCode -> {
                _uiState.update { it.copy(verificationCode = event.code) }
            }

            is MyInfoEditEvent.VerifyCode -> {
                verifyCode(event.phone, event.code)
            }

            is MyInfoEditEvent.ChangePassword -> {
                // TODO: 비밀번호 변경 로직
            }

            is MyInfoEditEvent.SaveInfo -> {
                saveInfo()
            }
        }
    }

    /**
     * 인증번호 발송
     */
    private fun sendVerificationCode(phone: String) {
        Log.d(TAG, "sendVerificationCode() called with phone: $phone")
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                Log.d(TAG, "Calling sendVerificationCode API")

                // API 호출
                userRepository.sendVerificationCode(phone).getOrThrow()

                Log.d(TAG, "sendVerificationCode API success - changing state to Verifying")
                _uiState.update {
                    it.copy(
                        phoneUiState = PhoneUiState.Verifying,
                        verificationResult = null,
                        isLoading = false
                    )
                }
                Log.d(TAG, "Current phoneUiState: ${_uiState.value.phoneUiState}")

                startTimer(180)
            } catch (e: Exception) {
                Log.e(TAG, "sendVerificationCode failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "인증번호 발송에 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 인증번호 재발송
     */
    private fun resendVerificationCode(phone: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // API 호출
                userRepository.sendVerificationCode(phone).getOrThrow()

                _uiState.update {
                    it.copy(
                        verificationResult = null,
                        isLoading = false
                    )
                }

                startTimer(180)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "인증번호 재발송에 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 인증번호 확인
     */
    private fun verifyCode(phone: String, code: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // API 호출
                val verificationToken = userRepository.verifyCode(phone, code).getOrThrow()

                _uiState.update {
                    it.copy(
                        verificationResult = true,
                        verificationToken = verificationToken,
                        isLoading = false
                    )
                }

                stopTimer()
                // 인증 성공 시 새 전화번호로 업데이트 (실제 저장은 나중에)
                _uiState.update {
                    it.copy(
                        phone = phone,
                        phoneUiState = PhoneUiState.Idle
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        verificationResult = false,
                        isLoading = false,
                        error = "인증번호 확인에 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 정보 저장
     */
    private fun saveInfo() {
        Log.d(TAG, "saveInfo() called")
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null, isSaveSuccess = false) }

                val currentState = _uiState.value
                Log.d(TAG, "Current state - name: ${currentState.name}, newPhone: ${currentState.newPhone}, verificationToken: ${currentState.verificationToken}")

                // 전화번호 변경 여부 확인
                val phoneChanged = currentState.newPhone.isNotBlank() &&
                                   currentState.verificationResult == true &&
                                   currentState.verificationToken != null

                Log.d(TAG, "Phone changed: $phoneChanged")

                // UpdateUserUseCase 호출
                updateUserUseCase(
                    name = currentState.name,
                    birth = currentState.birth,
                    phoneNumber = if (phoneChanged) currentState.newPhone else null,
                    verificationToken = if (phoneChanged) currentState.verificationToken else null,
                    profileImage = null // TODO: 프로필 이미지 선택 기능 구현 후 추가
                ).getOrThrow()

                Log.d(TAG, "saveInfo() - API 호출 성공")
                _uiState.update { it.copy(isLoading = false, isSaveSuccess = true) }
                Log.d(TAG, "isSaveSuccess set to true")
            } catch (e: Exception) {
                Log.e(TAG, "saveInfo() failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "정보 저장에 실패했습니다: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 타이머 시작
     */
    private fun startTimer(totalSeconds: Int) {
        stopTimer()
        _uiState.update { it.copy(secondsLeft = totalSeconds) }

        timerJob = viewModelScope.launch {
            while (_uiState.value.secondsLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(secondsLeft = it.secondsLeft - 1) }
            }
        }
    }

    /**
     * 타이머 중지
     */
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
