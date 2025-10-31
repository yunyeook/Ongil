package kr.co.ongil.presentation.ui.navigation

// 앱 내 라우트(경로)를 한 곳에서 관리
// React의 router/routes/어쩌구.tsx 같은거임
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    // 즐겨찾기 메인 화면
    data object Favorite : Screen("favorite")

    // 장소 상세 화면
    // ex) place_detail/장소식별자 이런 식으로 네비게이션할 예정
    data object PlaceDetail : Screen("place_detail/{placeId}") {

        fun createRoute(placeId: Long): String = "place_detail/$placeId"
    }

}