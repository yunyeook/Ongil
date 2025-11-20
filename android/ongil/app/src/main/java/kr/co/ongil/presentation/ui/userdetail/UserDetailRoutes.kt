package kr.co.ongil.presentation.ui.userdetail

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class UserDetailRoutes(val route: String) {
    // UserDetail 화면 경로 (relationshipId와 userType을 인자로 받음)
    data object Detail : UserDetailRoutes("user_detail/{relationshipId}/{userType}") {
        fun createRoute(relationshipId: Long, userType: String = "GUARDIAN"): String {
            return "user_detail/$relationshipId/$userType"
        }

        val arguments = listOf(
            navArgument("relationshipId") { type = NavType.LongType },
            navArgument("userType") { type = NavType.StringType; defaultValue = "GUARDIAN" }
        )
    }
}
