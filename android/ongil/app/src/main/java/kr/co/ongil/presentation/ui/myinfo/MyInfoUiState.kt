package kr.co.ongil.presentation.ui.myinfo

/**
 * 나의 정보 화면 UI 상태
 */
data class MyInfoUiState(
    val name: String = "",
    val phoneNumber: String = "",  // 이미 포맷팅된 전화번호
    val profileImage: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)