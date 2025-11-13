package kr.co.ongil.presentation.ui.favorite

// 화면에서 발생할 수 있는 모든 사용자 액션을 정의
sealed interface FavoriteUiEvent {
    data class OnTabSelected(val tab: FavoriteTab) : FavoriteUiEvent
    data class OnCallClick(val patientId: Long) : FavoriteUiEvent
    data class OnPatientCardClick(val patientId: Long) : FavoriteUiEvent
    data object onGoSearchUserClick : FavoriteUiEvent
    data object OnAddPlaceClick : FavoriteUiEvent
}