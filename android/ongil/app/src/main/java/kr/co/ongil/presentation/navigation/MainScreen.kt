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

/**
 * 앱의 메인 화면 - Scaffold + BottomBar + NavGraph 조합
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.MyInfo.route

    // BottomBar를 표시할 화면들
    val bottomBarRoutes = listOf(
        Routes.Location.route,
        Routes.Favorite.route,
        Routes.Home.route,
        Routes.PatientList.route,
        Routes.MyInfo.route
    )

    // 현재 라우트가 BottomBar를 표시해야 하는 화면인지 확인
    val showBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        containerColor = Color.White,
        bottomBar = {
            if (showBottomBar) {
                OngilBottomBar(
                    selectedRoute = currentRoute,
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
