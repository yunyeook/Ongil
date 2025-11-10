package kr.co.ongil.presentation.ui.auth

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.ongil.presentation.navigation.Routes

fun NavGraphBuilder.loginGraph(
    navController: NavHostController
) {
    composable(Routes.Login.route) {
        LoginRoute(
            onLoginSuccess = {
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Login.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            onClickFindPw = { navController.navigate(Routes.ChangePassword.route) },
            onClickSignup = { navController.navigate(Routes.Register.route) }
        )
    }
}
