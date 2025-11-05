package kr.co.ongil.presentation.navigation

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * 앱 내 화면 라우트 정의
 */
sealed class Routes(val route: String) {
    // 메인 화면들 (Bottom Navigation)
    object Location : Routes("location")
    object Home : Routes("home")
    object PatientList : Routes("patient_list")
    object RegisterUser : Routes("register_user")

    // 로그인
    object Login : Routes("login")

    // 회원가입
    object Signup : Routes("signup")

    // 나의 정보
    object MyInfo : Routes("my_info")

    // 내 정보 수정
    object EditInfo : Routes("edit_info")

    // 최근 통화목록
    object CallHistory : Routes("call_history")

    // 즐겨찾기
    // MOVED: FavoriteRoutes.kt 참조
    object Favorite : Routes("favorite")

    // 사용자 등록(검색)
    // MOVED: SearchUserRoutes.kt 참조
    object SearchUser : Routes("search_user")

    // 비밀번호 변경
    object ChangePassword : Routes("change_password")

    // 알림
    object Notifications : Routes("notifications")

    // 통화 상세
    object CallDetail : Routes("call_detail/{callLogId}") {
        fun createRoute(callLogId: Long): String = "call_detail/$callLogId"
    }

    // 환자 상세
    // MOVED: PatientDetailRoutes.kt 참조
    object PatientDetail : Routes("patient_detail/{patientId}/{name}/{phoneNumber}") {
        fun createRoute(
            patientId: Long,
            name: String,
            phoneNumber: String
        ): String {
            val encodedName = Uri.encode(name)
            val encodedPhone = Uri.encode(phoneNumber)
            return "patient_detail/$patientId/$encodedName/$encodedPhone"
        }

        val arguments = listOf(
            navArgument("patientId") { type = NavType.LongType },
            navArgument("name") { type = NavType.StringType },
            navArgument("phoneNumber") { type = NavType.StringType }
        )
    }

    // 장소 상세
    // MOVED: PlaceDetailRoutes.kt 참조
    object PlaceDetail : Routes("place_detail/{patientId}/{favoriteId}") {
        fun createRoute(patientId: Long, favoriteId: Long): String {
            return "place_detail/$patientId/$favoriteId"
        }

        val arguments = listOf(
            navArgument("patientId") { type = NavType.LongType },
            navArgument("favoriteId") { type = NavType.LongType }
        )
    }
}