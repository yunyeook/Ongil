package kr.co.ongil.presentation.ui.placedetail

import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class PlaceDetailRoutes(val route: String) {
    // PlaceDetail 화면 경로 (patientId, favoriteId, userType을 인자로 받음)
    data object Detail : PlaceDetailRoutes("place_detail/{patientId}/{favoriteId}/{userType}") {
        fun createRoute(patientId: Long, favoriteId: Long, userType: String = "GUARDIAN"): String {
            return "place_detail/$patientId/$favoriteId/$userType"
        }

        val arguments = listOf(
            navArgument("patientId") { type = NavType.LongType },
            navArgument("favoriteId") { type = NavType.LongType },
            navArgument("userType") { type = NavType.StringType; defaultValue = "GUARDIAN" }
        )
    }
}
