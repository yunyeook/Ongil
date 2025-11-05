package kr.co.ongil.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kr.co.ongil.presentation.ui.common.OngilTopBarForRoute
import kr.co.ongil.presentation.ui.common.bottomnav.OngilBottomBar
import kr.co.ongil.presentation.ui.common.OngilBrandHeaderCard

/**
 * 앱의 메인 화면 - Scaffold + BottomBar + NavGraph 조합
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Home.route
    val baseRoute = currentRoute
        .substringBefore("/")
        .substringBefore("?")

    // BottomBar를 표시할 화면들 (모든 페이지)
    val bottomBarRoutes = listOf(
        Routes.Location.route,
        Routes.Favorite.route,
        Routes.Home.route,
        Routes.PatientList.route,
        Routes.MyInfo.route,
        Routes.EditInfo.route,
        Routes.CallHistory.route,
        Routes.ChangePassword.route,
        Routes.SearchUser.route
    )

    // ✅ baseRoute를 기준으로 BottomBar 표시 여부 결정
    val showBottomBar = baseRoute in bottomBarRoutes || currentRoute.startsWith("call_detail/")

    // TopBar를 표시할 화면 (알림 화면은 자체 TopBar 사용)
    val showTopBar = baseRoute != Routes.Notifications.route

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = Color.White,
        topBar = {
            Box(modifier = Modifier.statusBarsPadding()) {
                if (showTopBar) {
                    if (baseRoute == Routes.Home.route) {
                        OngilBrandHeaderCard(
                            onBellClick = {
                                navController.navigate(Routes.Notifications.route) {
                                    launchSingleTop = true
                                }
                            },
                            profileImageUrl = null // TODO: 프로필 이미지 URL 연동
                        )
                    } else {
                        OngilTopBarForRoute(
                            route = baseRoute,
                            onBackClick = { navController.popBackStack() },
                            onBellClick = {
                                navController.navigate(Routes.Notifications.route) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                OngilBottomBar(
                    selectedRoute = baseRoute,
                    onClick = { route ->
                        val navigationRoute = if (route == Routes.Favorite.route) {
                            "${Routes.Favorite.route}/1"
                        } else {
                            route
                        }
                        navController.navigate(navigationRoute) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues).imePadding(),
            startDestination = Routes.MyInfo.route
        )
    }
}
