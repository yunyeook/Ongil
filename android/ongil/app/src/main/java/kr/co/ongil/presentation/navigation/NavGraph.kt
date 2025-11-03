package kr.co.ongil.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kr.co.ongil.presentation.ui.myinfo.MyInfoEditScreen
import kr.co.ongil.presentation.ui.myinfo.MyInfoScreen
import kr.co.ongil.presentation.ui.signup.SignupScreen
import kr.co.ongil.presentation.ui.signup.SignupViewModel
import kr.co.ongil.presentation.ui.myinfo.ChangePasswordScreen
import kr.co.ongil.presentation.ui.myinfo.RecentCallsScreen
import kr.co.ongil.presentation.ui.myinfo.CallDetailScreen
import kr.co.ongil.presentation.ui.notification.NotificationScreen
import kr.co.ongil.presentation.viewmodel.MyInfoViewModel
import kr.co.ongil.presentation.ui.common.OngilTopBarForRoute
import kr.co.ongil.presentation.ui.common.OngilBrandHeaderCard

/**
 * 앱 Navigation Graph
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.MyInfo.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 위치 화면
        composable(Routes.Location.route) {
            Column(modifier = Modifier.fillMaxSize()) {
                OngilBrandHeaderCard(
                    onBellClick = { navController.navigate(Routes.Notifications.route) }
                )
                PlaceholderScreen("위치")
            }
        }

        // 즐겨찾기 화면
        composable(Routes.Favorite.route) {
            Column(modifier = Modifier.fillMaxSize()) {
                OngilBrandHeaderCard(
                    onBellClick = { /* TODO: 알림 화면 이동 */ }
                )
                PlaceholderScreen("즐겨찾기")
            }
        }

        // 홈 화면
        composable(Routes.Home.route) {
            Column(modifier = Modifier.fillMaxSize()) {
                OngilBrandHeaderCard(
                    onBellClick = { navController.navigate(Routes.Notifications.route) }
                )
                PlaceholderScreen("홈")
            }
        }

        // 환자 정보 화면
        composable(Routes.PatientList.route) {
            Column(modifier = Modifier.fillMaxSize()) {
                OngilBrandHeaderCard(
                    onBellClick = { navController.navigate(Routes.Notifications.route) }
                )
                PlaceholderScreen("환자 정보")
            }
        }

        // 사용자 찾기 화면
        composable(Routes.SearchUser.route) {
            Column(modifier = Modifier.fillMaxSize()) {
                OngilTopBarForRoute(
                    route = Routes.SearchUser.route,
                    onBackClick = { navController.popBackStack() },
                    onBellClick = { navController.navigate(Routes.Notifications.route) }
                )
                PlaceholderScreen("사용자 찾기")
            }
        }

        // 나의 정보 화면
        composable(Routes.MyInfo.route) {
            val viewModel: MyInfoViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            Column(modifier = Modifier.fillMaxSize()) {
                OngilBrandHeaderCard(
                    onBellClick = { navController.navigate(Routes.Notifications.route) },
                    profileImageUrl = uiState.profileImage
                )
                MyInfoScreen(
                    uiState = uiState,
                    onEditInfo = {
                        navController.navigate(Routes.EditInfo.route)
                    },
                    onRecentCalls = {
                        navController.navigate(Routes.CallHistory.route)
                    },
                    onLogout = {
                        viewModel.logout()
                        // TODO: 로그아웃 성공시 로그인 화면으로 이동
                        // navController.navigate(Routes.Login.route) {
                        //     popUpTo(Routes.MyInfo.route) { inclusive = true }
                        // }
                    }
                )
            }
        }

        // 내 정보 수정 화면
        composable(Routes.EditInfo.route) {
            val editViewModel: kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel = viewModel()

            Column(modifier = Modifier.fillMaxSize()) {
                OngilTopBarForRoute(
                    route = Routes.EditInfo.route,
                    onBackClick = { navController.popBackStack() },
                    onBellClick = { navController.navigate(Routes.Notifications.route) }
                )
                MyInfoEditScreen(
                    viewModel = editViewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onChangePasswordClick = {
                        navController.navigate(Routes.ChangePassword.route)
                    }
                )
            }
        }

        // 최근 통화목록 화면
        composable(Routes.CallHistory.route) {
            Column(modifier = Modifier.fillMaxSize()) {
                OngilTopBarForRoute(
                    route = Routes.CallHistory.route,
                    onBackClick = { navController.popBackStack() },
                    onBellClick = { navController.navigate(Routes.Notifications.route) }
                )
                RecentCallsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToCallDetail = { callId ->
                        navController.navigate(Routes.CallDetail.createRoute(callId))
                    }
                )
            }
        }

        // 비밀번호 변경 화면
        composable(Routes.ChangePassword.route) {
            Column(modifier = Modifier.fillMaxSize()) {
                OngilTopBarForRoute(
                    route = Routes.ChangePassword.route,
                    onBackClick = { navController.popBackStack() },
                    onBellClick = { navController.navigate(Routes.Notifications.route) }
                )
                ChangePasswordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onPasswordChanged = {
                        // 비밀번호 변경 성공 시 추가 동작이 필요하면 여기에 구현
                    }
                )
            }
        }

        // 통화 상세 화면
        composable(
            route = Routes.CallDetail.route,
            arguments = listOf(
                navArgument("callLogId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val callLogId = backStackEntry.arguments?.getLong("callLogId") ?: 0L

            Column(modifier = Modifier.fillMaxSize()) {
                OngilTopBarForRoute(
                    route = Routes.CallDetail.route,
                    onBackClick = { navController.popBackStack() },
                    onBellClick = { navController.navigate(Routes.Notifications.route) }
                )
                CallDetailScreen(
                    callLogId = callLogId
                )
            }
        }

        // 알림 화면
        composable(Routes.Notifications.route) {
            Column(modifier = Modifier.fillMaxSize()) {
                NotificationScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
        // 회원가입 화면
        composable(Routes.Signup.route) {
            val viewModel: SignupViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            SignupScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onProfileImageClick = viewModel::onProfileImageClick,
                onNameChange = viewModel::onNameChange,
                onBirthClick = viewModel::onBirthClick,
                onPhoneChange = viewModel::onPhoneChange,
                onRequestVerificationCode = viewModel::onRequestVerificationCode,
                onVerificationCodeChange = viewModel::onVerificationCodeChange,
                onVerifyCodeClick = viewModel::onVerifyCodeClick,
                onPasswordChange = viewModel::onPasswordChange,
                onTogglePasswordVisible = viewModel::onTogglePasswordVisible,
                onPasswordConfirmChange = viewModel::onPasswordConfirmChange,
                onTogglePasswordConfirmVisible = viewModel::onTogglePasswordConfirmVisible,
                onSelectGuardian = viewModel::onSelectGuardian,
                onSelectPatient = viewModel::onSelectPatient,
                onSubmitSignup = viewModel::onSubmitSignup,
                onDismissSuccessModal = viewModel::onDismissSuccessModal,
                onDismissErrorModal = viewModel::onDismissErrorModal,
            )
        }
    }
}

/**
 * 임시 화면 (실제 화면 구현 전까지 사용)
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title 화면\n(구현 예정)",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}