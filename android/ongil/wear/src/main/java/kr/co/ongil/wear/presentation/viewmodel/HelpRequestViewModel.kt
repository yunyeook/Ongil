package kr.co.ongil.wear.presentation.viewmodel

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.wear.data.datasource.sync.PhoneDataSyncManager
import kr.co.ongil.wear.domain.usecase.RequestHelpUseCase
import java.util.Locale
import javax.inject.Inject

/**
 * 도움 요청 ViewModel
 *
 * 주요 기능:
 * 1. TTS 음성 재생 ("도와주세요!" 등)
 * 2. Phone으로 SOS 알림 전송
 * 3. Phone에서 온 도움 요청 수신 시 TTS 재생
 * 4. UI 상태 관리
 */
@HiltViewModel
class HelpRequestViewModel @Inject constructor(
    application: Application,
    private val requestHelpUseCase: RequestHelpUseCase,
    private val phoneDataSyncManager: PhoneDataSyncManager
) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HelpRequestViewModel"
        private const val DEFAULT_HELP_MESSAGE = "도와주세요! 길을 잃었습니다."
    }

    // === UI 상태 ===

    data class UiState(
        val isLoading: Boolean = false,
        val isTtsSpeaking: Boolean = false,
        val isHelpRequestSent: Boolean = false,
        val errorMessage: String? = null,
        val ttsInitialized: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // === TTS 초기화 ===

    private var textToSpeech: TextToSpeech? = null

    init {
        initializeTts()
        setupPhoneHelpRequestListener()
    }

    /**
     * TTS 초기화
     */
    private fun initializeTts() {
        textToSpeech = TextToSpeech(getApplication()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.KOREAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "한국어 TTS 지원 안됨, 기본 언어 사용")
                    textToSpeech?.language = Locale.getDefault()
                }
                _uiState.update { it.copy(ttsInitialized = true) }
                Log.d(TAG, "TTS 초기화 성공")
            } else {
                Log.e(TAG, "TTS 초기화 실패")
                _uiState.update {
                    it.copy(
                        ttsInitialized = false,
                        errorMessage = "음성 재생 기능을 사용할 수 없습니다"
                    )
                }
            }
        }
    }

    /**
     * Phone에서 온 도움 요청 리스너 설정
     *
     * 보호자가 Phone 앱에서 도움 요청 버튼을 누르면
     * Watch에서 TTS 음성이 재생됨
     */
    private fun setupPhoneHelpRequestListener() {
        phoneDataSyncManager.setOnHelpRequestReceivedListener { message ->
            Log.d(TAG, "보호자로부터 도움 요청 수신: $message")
            // TTS 음성 재생
            speakMessage(message)
        }
    }

    /**
     * 도움 요청 실행
     *
     * @param message 도움 요청 메시지 (기본값: "도와주세요! 길을 잃었습니다.")
     */
    fun requestHelp(message: String = DEFAULT_HELP_MESSAGE) {
        viewModelScope.launch {
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        errorMessage = null,
                        isHelpRequestSent = false
                    )
                }

                Log.d(TAG, "도움 요청 시작: $message")

                // 1. TTS 음성 재생
                speakMessage(message)

                // 2. Phone으로 SOS 알림 전송
                val result = requestHelpUseCase(message)

                if (result.isSuccess) {
                    Log.d(TAG, "✓ 도움 요청 성공")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isHelpRequestSent = true,
                            errorMessage = null
                        )
                    }
                } else {
                    val error = result.exceptionOrNull()
                    Log.e(TAG, "도움 요청 실패: ${error?.message}")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isHelpRequestSent = false,
                            errorMessage = error?.message ?: "도움 요청 전송에 실패했습니다"
                        )
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "도움 요청 중 오류", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isHelpRequestSent = false,
                        errorMessage = e.message ?: "오류가 발생했습니다"
                    )
                }
            }
        }
    }

    /**
     * TTS 음성 재생
     *
     * @param message 재생할 메시지
     */
    private fun speakMessage(message: String) {
        if (textToSpeech == null) {
            Log.w(TAG, "TTS가 초기화되지 않음")
            return
        }

        _uiState.update { it.copy(isTtsSpeaking = true) }

        // TTS 재생
        textToSpeech?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "HELP_REQUEST_TTS"
        )

        // TTS 완료 리스너 설정
        textToSpeech?.setOnUtteranceProgressListener(object :
            android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "TTS 재생 시작")
            }

            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "TTS 재생 완료")
                _uiState.update { it.copy(isTtsSpeaking = false) }
            }

            override fun onError(utteranceId: String?) {
                Log.e(TAG, "TTS 재생 오류")
                _uiState.update { it.copy(isTtsSpeaking = false) }
            }
        })
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * 도움 요청 완료 상태 초기화
     */
    fun resetHelpRequestStatus() {
        _uiState.update { it.copy(isHelpRequestSent = false) }
    }

    /**
     * TTS 정리
     */
    override fun onCleared() {
        super.onCleared()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        Log.d(TAG, "TTS 정리 완료")
    }
}
