package kr.co.ongil.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kr.co.ongil.presentation.ui.common.OngilTopBarForRoute
import kr.co.ongil.presentation.ui.common.bottomnav.OngilBottomBar
import kr.co.ongil.presentation.ui.common.OngilBrandHeaderCard
import kr.co.ongil.presentation.ui.auth.AuthStateViewModel

@Composable
fun MainScreen(
    authViewModel: AuthStateViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUserId by authViewModel.currentUserId.collectAsState()
    val currentUserInfo by authViewModel.currentUserInfo.collectAsState(initial = null)

    // 사용자 타입 추출
    val userType = currentUserInfo?.getOrNull()?.userType ?: ""

    // 로그인 상태 확인 중
    if (isLoggedIn == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Home.route
    val baseRoute = currentRoute.substringBefore("/").substringBefore("?")

    // ✅ 인증 관련 라우트(앱 크롬 숨김 대상)
    val authRoutes = setOf(
        Routes.Login.route,
        Routes.Register.route,
        Routes.ChangePassword.route
    )

    // BottomBar를 표시할 화면들 (메인 탭 위주)
    val bottomBarRoutes = listOf(
        Routes.Location.route,
        Routes.Favorite.route,
        Routes.Home.route,
        Routes.PatientList.route,
        Routes.MyInfo.route
        // 필요하면 여기에만 탭 대상 추가: EditInfo/CallHistory/SearchUser 등은 보통 탭 아님
    )

    // ✅ 인증 화면에서는 BottomBar 숨김
    val showBottomBar = baseRoute in bottomBarRoutes && baseRoute !in authRoutes

    // ✅ 인증 화면/알림 화면에서는 TopBar 숨김(알림은 자체 TopBar)
    val showTopBar = baseRoute !in authRoutes && baseRoute != Routes.Notifications.route

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = Color.Transparent,
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
                            },
                            userType = userType
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
                        val navigationRoute =
                            if (route == Routes.Favorite.route) {
                                // 현재 로그인한 사용자의 ID를 사용 (환자는 자신의 즐겨찾기 조회)
                                val userId = currentUserInfo?.getOrNull()?.id?.toLong() ?: 0L
                                "${Routes.Favorite.route}/$userId"
                            }
                            else route
                        navController.navigate(navigationRoute) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->

//        AppNavGraph(
//            navController = navController,
//            modifier = Modifier.padding(paddingValues).imePadding(),
//            startDestination = if (isLoggedIn == true) Routes.Home.route else Routes.Login.route
//        )
        AppNavGraph(
            navController = navController,
            // ✅ AppNavGraph가 화면 전체를 채우도록 하고, 패딩은 내부로 전달합니다.
            //    Modifier에서 padding을 제거하고 fillMaxSize()를 적용합니다.
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            paddingValues = paddingValues, // ✅ paddingValues를 파라미터로 전달
            startDestination = if (isLoggedIn == true) Routes.Home.route else Routes.Login.route
        )
    }
}
