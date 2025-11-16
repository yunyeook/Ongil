package kr.co.ongil.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kr.co.ongil.presentation.ui.myinfo.MyInfoEditScreen
import kr.co.ongil.presentation.ui.myinfo.MyInfoScreen
import kr.co.ongil.presentation.ui.myinfo.RecentCallsScreen
import kr.co.ongil.presentation.ui.myinfo.CallDetailScreen
import kr.co.ongil.presentation.ui.notification.NotificationScreen
import kr.co.ongil.presentation.viewmodel.MyInfoSideEffect
import kr.co.ongil.presentation.ui.home.HomeScreen
import kr.co.ongil.presentation.ui.favorite.favoriteGraph
import kr.co.ongil.presentation.ui.userdetail.userDetailGraph
import kr.co.ongil.presentation.ui.placedetail.placeDetailGraph
import kr.co.ongil.presentation.ui.auth.loginGraph
import kotlinx.coroutines.flow.collectLatest
import kr.co.ongil.presentation.ui.auth.register.registerGraph
import kr.co.ongil.presentation.ui.myinfo.ChangePasswordScreen
import kr.co.ongil.presentation.ui.map.MapScreen
import kr.co.ongil.presentation.ui.safezonesetting.safeZoneGraph
import kr.co.ongil.presentation.ui.home.homeGraph
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    startDestination: String = Routes.Login.route,
    authViewModel: kr.co.ongil.presentation.ui.auth.AuthStateViewModel? = null,
    onMapShowBarsChange: (Boolean) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier.background(Color.White)
    ) {
        // 홈
        homeGraph(navController, paddingValues)

        // 위치 - 지도 화면
        composable(Routes.Location.route) { backStackEntry ->
            val previousEntry = navController.previousBackStackEntry
            val searchPlaceholder = remember {
                val placeholder = previousEntry?.savedStateHandle?.get<String>("search_placeholder") ?: "장소를 검색해주세요"
                previousEntry?.savedStateHandle?.remove<String>("search_placeholder")
                placeholder
            }
            val requestSearchFocus = remember {
                val shouldFocus = previousEntry?.savedStateHandle?.get<Boolean>("request_search_focus") ?: false
                previousEntry?.savedStateHandle?.remove<Boolean>("request_search_focus")
                shouldFocus
            }

            MapScreen(
                navController = navController,
                paddingValues = paddingValues,
                authViewModel = authViewModel ?: hiltViewModel(),
                onShowBarsChange = onMapShowBarsChange,
                searchPlaceholder = searchPlaceholder,
                requestSearchFocus = requestSearchFocus
            )
        }

        // 환자 정보
        composable(Routes.PatientList.route) {
            kr.co.ongil.presentation.ui.patientinfo.PatientInfoScreen(
                modifier = Modifier.padding(paddingValues)
            )
        }


        // 사용자 검색 - 친구추가 화면
        composable(Routes.SearchUser.route) {
            val vm: kr.co.ongil.presentation.ui.searchuser.SearchUserViewModel = hiltViewModel()
            kr.co.ongil.presentation.ui.searchuser.SearchUserScreen(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                viewModel = vm
            )
        }

        loginGraph(navController)
        registerGraph(navController)

        // 즐겨찾기
        favoriteGraph(navController, paddingValues)
        // 사용자(환자/보호자) 상세
        userDetailGraph(navController, paddingValues)
        // 장소
        placeDetailGraph(navController, paddingValues)


        // 알림
        composable(Routes.Notifications.route) {
            NotificationScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }


        // 나의정보
        composable(Routes.MyInfo.route) {
            val viewModel: kr.co.ongil.presentation.viewmodel.MyInfoViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

            // 화면이 다시 표시될 때 정보 새로고침
            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        viewModel.loadUserInfo()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            // SideEffect 처리 (로그아웃 시 로그인 화면으로 이동)
            LaunchedEffect(Unit) {
                viewModel.sideEffect.collectLatest { effect ->
                    when (effect) {
                        is MyInfoSideEffect.NavigateToLogin -> {
                            // 모든 백스택 제거하고 로그인 화면으로 이동
                            navController.navigate(Routes.Login.route) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            }

            MyInfoScreen(
                uiState = uiState,
                onEditInfo = { navController.navigate(Routes.EditInfo.route) },
                onRecentCalls = { navController.navigate(Routes.CallHistory.route) },
                onLogout = { viewModel.logout() },
                modifier = Modifier.padding(paddingValues)
            )
        }


        // 나의정보 수정
        composable(Routes.EditInfo.route) {
            val editViewModel: kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel = hiltViewModel()
            MyInfoEditScreen(
                viewModel = editViewModel,
                onNavigateBack = {
                    // 내정보 화면으로 명시적으로 이동 (EditInfo 화면 제거)
                    navController.popBackStack(Routes.MyInfo.route, inclusive = false)
                },
                onChangePasswordClick = { navController.navigate(Routes.ChangePassword.route) }
            )
        }

        // 비밀번호 변경
        composable(Routes.ChangePassword.route) {
            val changePasswordViewModel: kr.co.ongil.presentation.viewmodel.ChangePasswordViewModel = hiltViewModel()
            ChangePasswordScreen(
                viewModel = changePasswordViewModel,
                onNavigateBack = { navController.popBackStack() },
                onPasswordChanged = {
                    // 비밀번호 변경 성공 시 내정보 화면으로 이동 (EditInfo 화면 건너뛰기)
                    navController.popBackStack(Routes.MyInfo.route, inclusive = false)
                }
            )
        }

        // 통화 내역
        composable(Routes.CallHistory.route) {
            val currentUserInfo by (authViewModel?.currentUserInfo ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(initial = null)
            val userType = currentUserInfo?.getOrNull()?.userType ?: "PATIENT"  // 기본값

            RecentCallsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCallDetail = { callId ->
                    navController.navigate(Routes.CallDetail.createRoute(callId))
                },
                onNavigateToVoipCall = { targetName, targetPhone, receiverId ->
                    // 재통화: isCaller = true, callId는 새로 생성됨
                    navController.navigate(
                        Routes.VoipCall.createRoute(
                            targetName = targetName,
                            targetPhone = targetPhone,
                            isCaller = true,
                            userType = userType,
                            callId = null,
                            receiverId = receiverId
                        )
                    )
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

        // VoIP 수신 화면
        composable(
            route = Routes.VoipIncomingCall.route,
            arguments = listOf(
                navArgument("callId") { type = NavType.StringType },
                navArgument("callerName") { type = NavType.StringType },
                navArgument("callerPhone") { type = NavType.StringType },
                navArgument("userType") { type = NavType.StringType },
                navArgument("sessionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val callId = backStackEntry.arguments?.getString("callId")?.toLongOrNull() ?: 0L
            val callerName = backStackEntry.arguments?.getString("callerName") ?: ""
            val callerPhone = backStackEntry.arguments?.getString("callerPhone") ?: ""
            val userType = backStackEntry.arguments?.getString("userType") ?: "PATIENT"
            val sessionId = backStackEntry.arguments?.getString("sessionId")

            kr.co.ongil.presentation.ui.call.VoipIncomingCallScreen(
                callerName = callerName,
                callerPhone = callerPhone,
                callId = callId,
                userType = userType,
                sessionId = sessionId,
                onAccepted = {
                    // 수락 시 통화 중 화면으로 이동
                    navController.navigate(
                        Routes.VoipCall.createRoute(
                            targetName = callerName,
                            targetPhone = callerPhone,
                            isCaller = false,
                            userType = userType,
                            callId = callId,
                            receiverId = null
                        )
                    ) {
                        popUpTo(Routes.VoipIncomingCall.route) { inclusive = true }
                    }
                },
                onDeclined = {
                    // 거절 시 이전 화면으로
                    navController.popBackStack()
                }
            )
        }

        // 안전구역 설정 (새로운 구조)
        authViewModel?.let { viewModel ->
            safeZoneGraph(navController, paddingValues, viewModel)
        }
        // VoIP 통화 중 화면
        composable(Routes.VoipCall.route) { backStackEntry ->
            val targetName = backStackEntry.arguments?.getString("targetName") ?: ""
            val targetPhone = backStackEntry.arguments?.getString("targetPhone") ?: ""
            val isCaller = backStackEntry.arguments?.getString("isCaller")?.toBoolean() ?: false
            val userType = backStackEntry.arguments?.getString("userType") ?: "PATIENT"
            val callId = backStackEntry.arguments?.getString("callId")?.toLongOrNull()
            val receiverId = backStackEntry.arguments?.getString("receiverId")?.toLongOrNull()

            kr.co.ongil.presentation.ui.call.VoipCallScreen(
                targetName = targetName,
                targetPhone = targetPhone,
                isCaller = isCaller,
                userType = userType,
                callId = callId?.takeIf { it > 0 },
                receiverId = receiverId?.takeIf { it > 0 },
                onCallEnded = {
                    // 통화 종료 시 이전 화면으로
                    navController.popBackStack()
                }
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