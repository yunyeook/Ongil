package kr.co.ongil.presentation.ui.placedetail

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class PlaceDetailRoutes(val route: String) {
    // PlaceDetail 화면 경로 (patientId와 favoriteId를 인자로 받음)
    data object Detail : PlaceDetailRoutes("place_detail/{patientId}/{favoriteId}") {
        fun createRoute(patientId: Long, favoriteId: Long): String {
            return "place_detail/$patientId/$favoriteId"
        }

        val arguments = listOf(
            navArgument("patientId") { type = NavType.LongType },
            navArgument("favoriteId") { type = NavType.LongType }
        )
    }
}
