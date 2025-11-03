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

    // 비밀번호 변경
    object ChangePassword : Routes("change_password")

    // 통화 상세
    object CallDetail : Routes("call_detail/{callLogId}") {
        fun createRoute(callLogId: Long): String = "call_detail/$callLogId"
    }

    // TODO: 다른 화면들 추가
}