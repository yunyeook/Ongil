// presentation/ui/auth/LoginViewModel.kt
package kr.co.ongil.presentation.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kr.co.ongil.domain.usecase.auth.LoginUseCase
import kr.co.ongil.BuildConfig
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

sealed interface LoginEffect {
    object NavigateHome : LoginEffect  // 홈 화면으로 이동
    data class ShowSnack(val message: String) : LoginEffect  // 스낵바 메시지 표시
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val userDataStoreManager: kr.co.ongil.data.datasource.local.preferences.UserDataStoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect

    // 전화번호 입력 처리
    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v).revalidate() }

    // 비밀번호 입력 처리
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v).revalidate() }

    // 로그인 버튼 클릭 처리
    fun onClickLogin() {
        val s = _state.value
        if (s.isLoading) return
        if (!s.isLoginEnabled) {
            viewModelScope.launch {
                _effect.emit(LoginEffect.ShowSnack("전화번호/비밀번호를 확인해주세요."))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }  // 로딩 상태로 업데이트

            // LoginUseCase를 통한 로그인 처리
            loginUseCase(phoneNumber = s.phone, password = s.password)
                .onSuccess { loginResponse ->
                    // 로그인 성공
                    // TODO: 토큰 저장 처리 (TokenManager에서 처리하도록 변경 필요)

                    // FCM 토큰 가져와서 서버로 전송
                    sendFcmTokenToServer()

                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(LoginEffect.NavigateHome)  // 홈 화면으로 이동
                }
                .onFailure { exception ->
                    // 로그인 실패
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(LoginEffect.ShowSnack(exception.message ?: "로그인에 실패했습니다."))
                }
        }
    }

    private fun LoginUiState.revalidate(): LoginUiState {
        val enabled = phone.length in 10..11 && password.length >= 4
        return copy(isLoginEnabled = enabled)
    }

    /**
     * 로그인 성공 시 FCM 토큰을 서버로 전송
     * 매 로그인마다 서버로 전송
     */
    private fun sendFcmTokenToServer() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM", "로그인 시 토큰 조회 성공: $fcmToken")

                // 매번 서버로 전송
                viewModelScope.launch {
                    sendTokenToBackend(fcmToken)
                    // 서버 전송 후 로컬에 저장
                    userDataStoreManager.saveFcmToken(fcmToken)
                }

                // TODO: 나중에 최적화 필요시 아래 로직 활용
                // 로컬에 저장된 토큰과 비교
//                viewModelScope.launch {
//                    val savedToken = userDataStoreManager.getFcmToken().firstOrNull()
//                    if (savedToken != fcmToken) {
//                        // 토큰이 변경되었거나 처음 저장하는 경우
//                        Log.d("FCM", "토큰이 변경됨 (기존: $savedToken -> 새: $fcmToken)")
//                        sendTokenToBackend(fcmToken)
//                        // 서버 전송 후 로컬에 저장
//                        userDataStoreManager.saveFcmToken(fcmToken)
//                    } else {
//                        Log.d("FCM", "토큰이 동일함, 서버 전송 스킵")
//                    }
//                }
            } else {
                Log.e("FCM", "로그인 시 토큰 조회 실패", task.exception)
            }
        }
    }

    /**
     * 백엔드 서버로 FCM 토큰 전송
     */
    private fun sendTokenToBackend(token: String) {
        viewModelScope.launch {
            // AccessToken 가져오기
            val accessToken = userDataStoreManager.getAccessToken().firstOrNull()
            if (accessToken == null) {
                Log.e("FCM", "AccessToken이 없어서 FCM 토큰 전송 실패")
                return@launch
            }

            val client = OkHttpClient()

            val json = JSONObject().apply {
                put("token", token)
            }

            val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url("${BuildConfig.BASE_URL}api/v1/fcm/register")
                .addHeader("Authorization", "Bearer $accessToken")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("FCM", "로그인 시 토큰 전송 실패: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        Log.d("FCM", "✅ 로그인 시 FCM 토큰 서버 전송 성공")
                    } else {
                        Log.e("FCM", "❌ 로그인 시 토큰 서버 응답 오류: ${response.code}")
                        // TODO: 토큰 전송 실패 처리
                        // 실패 하면 알림 토글 off
                        // 나중에 알림 토글 on 할때 다시 시도
                    }
                }
            })
        }
    }
}
