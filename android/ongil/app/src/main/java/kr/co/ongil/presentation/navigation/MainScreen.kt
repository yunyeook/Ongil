package kr.co.ongil.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kr.co.ongil.presentation.ui.common.bottomnav.OngilBottomBar
import kr.co.ongil.presentation.ui.common.OngilTopBarForRoute
import kr.co.ongil.presentation.ui.common.OngilBrandHeaderCard

/**
 * 앱의 메인 화면 - Scaffold + BottomBar + NavGraph 조합
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.MyInfo.route
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

    // 현재 라우트가 BottomBar를 표시해야 하는 화면인지 확인
    // CallDetail은 동적 라우트이므로 별도 체크
    val showBottomBar = currentRoute in bottomBarRoutes || currentRoute.startsWith("call_detail/")

    // TopBar를 표시할 화면 (알림 화면은 자체 TopBar 사용)
    val showTopBar = baseRoute != Routes.Notifications.route

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color.White,
        topBar = {
            if (showTopBar) {
                // 홈 화면은 로고 헤더, 나머지는 일반 TopBar
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
        },
        bottomBar = {
            if (showBottomBar) {
                OngilBottomBar(
                    selectedRoute = baseRoute,
                    onClick = { route ->
                        navController.navigate(route) {
                            // 같은 화면을 다시 클릭하면 스택을 정리
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // 중복 방지
                            launchSingleTop = true
                            // 상태 복원
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues),
            startDestination = Routes.MyInfo.route
        )
    }
}
