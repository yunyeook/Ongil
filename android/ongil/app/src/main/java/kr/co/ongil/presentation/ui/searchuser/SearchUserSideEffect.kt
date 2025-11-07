package kr.co.ongil.presentation.ui.searchuser

sealed interface SearchUserUiSideEffect {

    // 일회성 안내 메시지
    data class ShowToast(val message: String) : SearchUserUiSideEffect

    // 등록 완료 후 해당 사용자 상세로 이동
    data class NavigateToRegisterUser(val userId: String) : SearchUserUiSideEffect

    // 네비게이션 후 스낵바 또는 토스트용 일시적 확인 메시지
    data class ShowSnackBar(val message: String) : SearchUserUiSideEffect

    // 초기 검색화면으로 이동
    object BackToSearchStart : SearchUserUiSideEffect
}