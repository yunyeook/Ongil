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
import kr.co.ongil.wear.presentation.ui.call.CallScreen
import kr.co.ongil.wear.presentation.ui.call.IncomingCallScreen
import kr.co.ongil.wear.presentation.ui.navigation.NavigationScreen
import kr.co.ongil.wear.presentation.ui.help.HelpRequestScreen
import kr.co.ongil.wear.presentation.ui.dashboard.DashboardScreen
import kr.co.ongil.wear.presentation.ui.patient.PatientSelectionScreen

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

        // 네비게이션 화면 (경로 안내)
        composable(
            route = WearRoute.Navigation.route,
            arguments = listOf(
                navArgument("navigationId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val navigationId = backStackEntry.arguments?.getLong("navigationId") ?: 0L

            NavigationScreen(
                navigationId = navigationId,
                onNavigationEnded = {
                    navController.popBackStack()
                }
            )
        }

        // 통화 화면 (발신/통화 중)
        composable(
            route = WearRoute.Call.route,
            arguments = listOf(
                navArgument("targetUserId") { type = NavType.StringType },
                navArgument("targetName") { type = NavType.StringType },
                navArgument("targetPhone") { type = NavType.StringType },
                navArgument("isCaller") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val targetUserId = backStackEntry.arguments?.getString("targetUserId") ?: ""
            val targetName = backStackEntry.arguments?.getString("targetName") ?: ""
            val targetPhone = backStackEntry.arguments?.getString("targetPhone") ?: ""
            val isCaller = backStackEntry.arguments?.getBoolean("isCaller") ?: true

            CallScreen(
                targetUserId = targetUserId,
                targetName = targetName,
                targetPhone = targetPhone,
                isCaller = isCaller,
                onCallEnded = {
                    navController.popBackStack()
                }
            )
        }

        // 수신 통화 화면
        composable(
            route = WearRoute.IncomingCall.route,
            arguments = listOf(
                navArgument("callId") { type = NavType.LongType },
                navArgument("callerUserId") { type = NavType.StringType },
                navArgument("callerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getLong("callId") ?: 0L
            val callerUserId = backStackEntry.arguments?.getString("callerUserId") ?: ""
            val callerName = backStackEntry.arguments?.getString("callerName") ?: ""

            IncomingCallScreen(
                callId = callId,
                callerUserId = callerUserId,
                callerName = callerName,
                onCallAccepted = {
                    // 수락 후 CallScreen으로 이동
                    navController.navigate(
                        WearRoute.Call.createRoute(
                            targetUserId = callerUserId,
                            targetName = callerName,
                            targetPhone = "",
                            isCaller = false
                        )
                    ) {
                        // IncomingCallScreen 제거
                        popUpTo(WearRoute.IncomingCall.route) {
                            inclusive = true
                        }
                    }
                },
                onCallRejected = {
                    navController.popBackStack()
                }
            )
        }

        // 도움 요청 화면
        composable(WearRoute.HelpRequest.route) {
            HelpRequestScreen(
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        // 환자 선택 화면 (보호자용)
        composable(WearRoute.PatientSelection.route) {
            PatientSelectionScreen(
                onPatientSelected = {
                    navController.popBackStack()
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        // 설정 화면
        composable(WearRoute.Settings.route) {
            // TODO: SettingsScreen 구현
            MainScreen() // 임시
        }
    }
}
