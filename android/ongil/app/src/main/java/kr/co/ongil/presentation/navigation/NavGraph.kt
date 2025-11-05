package kr.co.ongil.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
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
import kr.co.ongil.presentation.ui.favorite.FavoriteScreen
import kr.co.ongil.presentation.ui.searchuser.SearchUserScreen
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailViewModel
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailScreen
import kr.co.ongil.presentation.ui.home.HomeScreen
import kr.co.ongil.presentation.ui.searchuser.SearchUserViewModel
import kr.co.ongil.presentation.ui.favorite.favoriteGraph
//import kr.co.ongil.presentation.ui.patientdetail.patientGraph
import kr.co.ongil.presentation.ui.placedetail.placeDetailGraph
import kr.co.ongil.presentation.ui.auth.loginGraph
/**
 * 앱 Navigation Graph
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = "favorite_graph"
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 홈
        composable(Routes.Home.route) {
            HomeScreen(
                onGoSearchUserClick = { navController.navigate(Routes.SearchUser.route) },
                onGoSignupClick = { navController.navigate(Routes.Signup.route) }
            )
        }
        // 위치 - 아마도 지도
        composable(Routes.Location.route) { PlaceholderScreen("위치") }


        // 사용자 검색 - 친구추가 화면
        composable(Routes.SearchUser.route) {
            val vm: kr.co.ongil.presentation.ui.searchuser.SearchUserViewModel =
                androidx.lifecycle.viewmodel.compose.viewModel()
            kr.co.ongil.presentation.ui.searchuser.SearchUserScreen(
                navController = navController,
                viewModel = vm
            )
        }

        loginGraph(navController)

        // 즐겨찾기
        favoriteGraph(navController)
        // 환자
//        patientGraph(navController)
        // 장소
        placeDetailGraph(navController)

        // 알림
        composable(Routes.Notifications.route) {
            NotificationScreen(onNavigateBack = { navController.navigate(Routes.MyInfo.route) })
        }


        // 나의정보
        composable(Routes.MyInfo.route) {
            val viewModel: kr.co.ongil.presentation.viewmodel.MyInfoViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            MyInfoScreen(
                uiState = uiState,
                onEditInfo = { navController.navigate(Routes.EditInfo.route) },
                onRecentCalls = { navController.navigate(Routes.CallHistory.route) },
                onLogout = { viewModel.logout() }
            )
        }


        // 나의정보 수정
        composable(Routes.EditInfo.route) {
            val editViewModel: kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel = viewModel()
            MyInfoEditScreen(
                viewModel = editViewModel,
                onNavigateBack = { navController.popBackStack() },
                onChangePasswordClick = { navController.navigate(Routes.ChangePassword.route) }
            )
        }


        // 통화 내역
        composable(Routes.CallHistory.route) {
            RecentCallsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCallDetail = { callId ->
                    navController.navigate(Routes.CallDetail.createRoute(callId))
                }
            )
        }
        // 통화 상세
        composable(
            route = Routes.CallDetail.route,
            arguments = listOf(navArgument("callLogId") { type = NavType.LongType })
        ) { entry ->
            val callLogId = entry.arguments?.getLong("callLogId") ?: 0L
            CallDetailScreen(callLogId = callLogId)
        }


        // 회원가입
        composable(Routes.Signup.route) {
            val viewModel: SignupViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            SignupScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onProfileImageClick = viewModel::onProfileImageClick,
                onNameChange = viewModel::onNameChange,
                onBirthClick = viewModel::onBirthClick,
                onSetBirth = viewModel::onSetBirth,
                onDismissDatePicker = viewModel::onDismissDatePicker,
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
        modifier = Modifier.fillMaxSize(), color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title 화면\n(구현 예정)",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}