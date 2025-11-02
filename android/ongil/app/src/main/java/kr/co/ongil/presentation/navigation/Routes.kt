package kr.co.ongil.presentation.navigation

/**
 * 앱 내 화면 라우트 정의
 */
sealed class Routes(val route: String) {
    // 나의 정보
    object MyInfo : Routes("my_info")

    // 내 정보 수정
    object EditInfo : Routes("edit_info")

    // 최근 통화목록
    object CallHistory : Routes("call_history")

    // TODO: 다른 화면들 추가
}