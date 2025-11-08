package kr.co.ongil.presentation.ui.userdetail

import android.net.Uri
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class UserDetailRoutes(val route: String) {
    // UserDetail 화면 경로 (relationshipId만 인자로 받음)
    data object Detail : UserDetailRoutes("user_detail/{relationshipId}") {
        fun createRoute(relationshipId: Long): String {
            return "user_detail/$relationshipId"
        }

        val arguments = listOf(
            navArgument("relationshipId") { type = NavType.LongType }
        )
    }
}
