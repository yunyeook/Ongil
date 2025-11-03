package kr.co.ongil.presentation.navigation

/**
 * 앱 내 화면 라우트 정의
 */
sealed class Routes(val route: String) {
    // 메인 화면들 (Bottom Navigation)
    object Location : Routes("location")
    object Favorite : Routes("favorite")
    object Home : Routes("home")
    object PatientList : Routes("patient_list")
    object SearchUser : Routes("search_user")
    object RegisterUser : Routes("register_user")

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

    // 환자 상세
    object PatientDetail : Routes("patient_detail/{patientId}/{name}/{phoneNumber}/{gender}") {
        fun createRoute(
            patientId: Long,
            name: String,
            phoneNumber: String,
            gender: String
        ): String = "patient_detail/$patientId/$name/$phoneNumber/$gender"
    }

    // 장소 상세
    object PlaceDetail : Routes("place_detail/{favoriteId}/{placeName}/{address}") {
        fun createRoute(
            favoriteId: Long,
            placeName: String,
            address: String
        ): String = "place_detail/$favoriteId/$placeName/$address"
    }
}