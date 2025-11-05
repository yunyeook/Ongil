// presentation/ui/auth/LoginGraph.kt
package kr.co.ongil.presentation.ui.auth

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.ongil.presentation.navigation.Routes

fun NavGraphBuilder.loginGraph(navController: NavHostController) {
    composable(Routes.Login.route) {
        LoginRoute(
            onLoginSuccess = {
                // 로그인 성공 후 홈 화면으로 이동
                navController.navigate(Routes.Home.route) {
                    popUpTo(Routes.Login.route) { inclusive = true } // 로그인 화면을 스택에서 제거
                    launchSingleTop = true // 중복 방지
                }
            },
            onClickFindPw = { navController.navigate(Routes.ChangePassword.route) },
            onClickSignup = { navController.navigate(Routes.Signup.route) }
        )
    }
}
