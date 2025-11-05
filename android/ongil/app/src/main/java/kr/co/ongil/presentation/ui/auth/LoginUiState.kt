package kr.co.ongil.presentation.ui.auth

data class LoginUiState(
    val phone: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginEnabled: Boolean = false
) {
    /** 입력값 길이에 따라 버튼 활성화 여부 갱신 */
    fun revalidate(): LoginUiState {
        val enabled = phone.length in 10..11 && password.length >= 4
        return copy(isLoginEnabled = enabled)
    }
}
