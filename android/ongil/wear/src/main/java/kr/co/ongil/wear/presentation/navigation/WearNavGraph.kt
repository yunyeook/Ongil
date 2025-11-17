package kr.co.ongil.wear.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import kr.co.ongil.wear.presentation.ui.LoginSyncScreen
import kr.co.ongil.wear.presentation.ui.MainScreen
import kr.co.ongil.wear.presentation.ui.map.MapScreen

/**
 * Wear OS 네비게이션 그래프
 *
 * SwipeDismissableNavHost를 사용하여
 * 스와이프로 뒤로가기 가능
 */
@Composable
fun WearNavGraph(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    SwipeDismissableNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 로그인 동기화 화면
        composable(WearRoute.LoginSync.route) {
            LoginSyncScreen()
        }

        // 메인 대시보드
        composable(WearRoute.Dashboard.route) {
            DashboardScreen(
                onNavigateToMap = {
                    navController.navigate(WearRoute.Map.route)
                },
                onNavigateToCall = {
                    // TODO: 핫라인 번호로 통화 시작
                },
                onNavigateToHelp = {
                    navController.navigate(WearRoute.HelpRequest.route)
                }
            )
        }

        // 지도 화면
        composable(WearRoute.Map.route) {
            MapScreen()
        }

        // 네비게이션 화면
        composable(
            route = WearRoute.Navigation.route,
            arguments = listOf(
                navArgument("navigationId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val navigationId = backStackEntry.arguments?.getLong("navigationId") ?: 0L
            // TODO: NavigationScreen 구현
            MainScreen() // 임시
        }

        // 통화 화면
        composable(
            route = WearRoute.Call.route,
            arguments = listOf(
                navArgument("callId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getLong("callId") ?: 0L
            // TODO: CallScreen 구현
            MainScreen() // 임시
        }

        // 수신 통화 화면
        composable(
            route = WearRoute.IncomingCall.route,
            arguments = listOf(
                navArgument("callId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getLong("callId") ?: 0L
            // TODO: IncomingCallScreen 구현
            MainScreen() // 임시
        }

        // 도움 요청 화면
        composable(WearRoute.HelpRequest.route) {
            // TODO: HelpRequestScreen 구현
            MainScreen() // 임시
        }

        // 환자 선택 화면
        composable(WearRoute.PatientSelection.route) {
            // TODO: PatientSelectionScreen 구현
            MainScreen() // 임시
        }

        // 설정 화면
        composable(WearRoute.Settings.route) {
            // TODO: SettingsScreen 구현
            MainScreen() // 임시
        }
    }
}

/**
 * 대시보드 화면 (임시 구현)
 * TODO: 별도 파일로 분리
 */
@Composable
fun DashboardScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToCall: () -> Unit,
    onNavigateToHelp: () -> Unit
) {
    // TODO: 실제 대시보드 UI 구현
    MainScreen() // 임시로 MainScreen 사용
}
