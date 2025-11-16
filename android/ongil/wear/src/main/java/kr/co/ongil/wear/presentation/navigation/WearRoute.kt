package kr.co.ongil.wear.presentation.navigation

/**
 * Wear OS 앱 네비게이션 라우트
 */
sealed class WearRoute(val route: String) {

    // 로그인 동기화 화면
    data object LoginSync : WearRoute("login_sync")

    // 메인 대시보드
    data object Dashboard : WearRoute("dashboard")

    // 지도 화면
    data object Map : WearRoute("map")

    // 네비게이션 화면 (경로 안내)
    data object Navigation : WearRoute("navigation/{navigationId}") {
        fun createRoute(navigationId: Long) = "navigation/$navigationId"
    }

    // 통화 화면 (발신/통화 중)
    data object Call : WearRoute("call/{callId}") {
        fun createRoute(callId: Long) = "call/$callId"
    }

    // 수신 통화 화면
    data object IncomingCall : WearRoute("incoming_call/{callId}") {
        fun createRoute(callId: Long) = "incoming_call/$callId"
    }

    // 도움 요청 화면
    data object HelpRequest : WearRoute("help_request")

    // 환자 선택 화면 (보호자용)
    data object PatientSelection : WearRoute("patient_selection")

    // 설정 화면
    data object Settings : WearRoute("settings")
}
