package kr.co.ongil.presentation.ui.favorite


sealed class FavoriteRoutes(val route: String) {
    // 1. Favorite Graph Route
    data object Graph : FavoriteRoutes("favorite_graph")

    // 2. FavoriteScreen의 경로 (시작 지점)
    // patientId를 인자로 받습니다.
    data object Home : FavoriteRoutes("favorite_screen/{patientId}") {
        fun createRoute(patientId: Long): String {
            return "favorite_screen/$patientId"
        }
    }

    // 3. PlaceDetail 경로 (patientId와 favoriteId를 인자로 받음)
    data object PlaceDetail : FavoriteRoutes("place_detail_screen/{patientId}/{favoriteId}") {
        fun createRoute(patientId: Long, favoriteId: Long): String {
            return "place_detail_screen/$patientId/$favoriteId"
        }
    }
}