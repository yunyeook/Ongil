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
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.ongil.domain.usecase.auth.LoginUseCase
import kr.co.ongil.BuildConfig
import kr.co.ongil.domain.FcmTokenManager
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

    fun onPhoneChange(v: String) = _state.update { it.copy(phone = v).revalidate() }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v).revalidate() }

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
            _state.update { it.copy(isLoading = true) }

            loginUseCase(phoneNumber = s.phone, password = s.password)
                .onSuccess { loginResponse ->
                    // ✅ FCM 코드 전부 제거 - MainActivity가 알아서 처리함

                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(LoginEffect.NavigateHome)
                }
                .onFailure { exception ->
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(LoginEffect.ShowSnack(exception.message ?: "로그인에 실패했습니다."))
                }
        }
    }

    private fun LoginUiState.revalidate(): LoginUiState {
        val enabled = phone.length in 10..11 && password.length >= 4
        return copy(isLoginEnabled = enabled)
    }

    private fun sendFcmTokenToServer() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result
                Log.d("FCM", "로그인 시 토큰 조회 성공: $fcmToken")

                viewModelScope.launch {
                    sendTokenToBackend(fcmToken)
                    userDataStoreManager.saveFcmToken(fcmToken)
                }
            }
        }
    }

    private suspend fun sendTokenToBackend(token: String) {
        withContext(Dispatchers.IO) {
            try {
                val accessToken = userDataStoreManager.getAccessToken().firstOrNull()
                if (accessToken == null) {
                    Log.e("FCM", "AccessToken이 없어서 FCM 토큰 전송 실패")
                    return@withContext
                }

                val client = OkHttpClient()
                val json = JSONObject().apply { put("token", token) }
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
                        }
                    }
                })
            } catch (e: Exception) {
                Log.e("FCM", "❌ 토큰 전송 중 오류", e)
            }
        }
    }
}