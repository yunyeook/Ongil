// 📄 VoipCallUiState.kt
package kr.co.ongil.presentation.ui.call

import kr.co.ongil.data.model.call.VoipCallDto

/**
 * VOIP 통화 화면 상태를 표현하는 데이터 클래스
 */
data class VoipCallUiState(
    val isLoading: Boolean = false,        // 로딩 중인지 여부
    val call: VoipCallDto? = null,         // 현재 통화 세션 정보
    val message: String? = null,           // 사용자 피드백 메시지
    val error: String? = null              // 에러 메시지
)