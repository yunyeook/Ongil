package kr.co.ongil.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import kr.co.ongil.presentation.IncomingCallData
import kr.co.ongil.presentation.ui.auth.AuthStateViewModel
import kr.co.ongil.presentation.theme.OngilThemeProvider
import kr.co.ongil.presentation.ui.common.OngilBrandHeaderCard
import kr.co.ongil.presentation.ui.common.OngilTopBarForRoute
import kr.co.ongil.presentation.ui.common.bottomnav.OngilBottomBar
import kr.co.ongil.presentation.ui.common.selection.PatientInfoUi

@Composable
fun MainScreen(
    incomingCallData: IncomingCallData? = null,
    onIncomingCallHandled: () -> Unit = {},
    emergencyCallData: kr.co.ongil.presentation.EmergencyCallData? = null,
    onEmergencyCallHandled: () -> Unit = {},
    authViewModel: AuthStateViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val currentUserInfo by authViewModel.currentUserInfo.collectAsState(initial = null)
    val patientList by authViewModel.patientList.collectAsState()
    val selectedPatientId by authViewModel.selectedPatientId.collectAsState()
    val currentUserProfileImage by authViewModel.currentUserProfileImage.collectAsState()
    val selectedPatientProfileImage by authViewModel.selectedPatientProfileImage.collectAsState()
    val patientProfileImages by authViewModel.patientProfileImages.collectAsState()

    val userType = currentUserInfo?.getOrNull()?.userType ?: "GUARDIAN"

    // 로그인 상태 확인 중이면 로딩만 보여주고 NavHost 생성 안 함
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

    // ✅ startDestination을 로그인 결과 기준으로 한 번만 고정
    val startDestination = remember(isLoggedIn) {
        if (isLoggedIn == true) Routes.Home.route else Routes.Login.route
    }

    // ✅ FCM 수신 통화: callId 기준으로 한 번만 VoipIncomingCall로 이동
    LaunchedEffect(incomingCallData?.callId) {
        val data = incomingCallData ?: return@LaunchedEffect

        val route = Routes.VoipIncomingCall.createRoute(
            callId = data.callId,
            callerName = data.callerName,
            callerPhone = data.callerPhone,
            userType = data.userType,
            sessionId = data.sessionId
        )

        // 이미 같은 라우트면 중복 네비게이션 방지 (선택적이지만 안전)
        if (navController.currentDestination?.route != route) {
            navController.navigate(route) {
                launchSingleTop = true
            }
        }

        // NavHost 초기화 방지용: navigation 이후 플래그만 정리
        delay(100)
        onIncomingCallHandled()
    }

    // ✅ 응급 전화: 경로 이탈 5분 경과 시 기본 보호자에게 자동 전화
    LaunchedEffect(emergencyCallData?.targetName, emergencyCallData?.targetPhone) {
        val data = emergencyCallData ?: return@LaunchedEffect

        val route = Routes.VoipCall.createRoute(
            targetName = data.targetName,
            targetPhone = data.targetPhone,
            isCaller = data.isCaller,
            userType = data.userType,
            callId = 0L, // 응급 전화는 callId 없음
            receiverId = data.receiverId.toLongOrNull() ?: 0L
        )

        android.util.Log.d("MainScreen", "🚨 응급 전화 네비게이션: $route")

        navController.navigate(route) {
            launchSingleTop = true
        }

        delay(100)
        onEmergencyCallHandled()
    }

    // 인증 관련 화면들 (Top/Bottom 숨김 대상)
    val authRoutes = setOf(
        Routes.Login.route,
        Routes.Register.route,
        Routes.ChangePassword.route
    )

    // 통화 관련 화면들 (Top/Bottom 숨김 대상)
    val callRoutes = setOf(
        "voip_incoming_call",  // VoipIncomingCall route의 baseRoute
        "voip_call"            // VoipCall route의 baseRoute
    )

    // BottomBar 표시 대상
    val bottomBarRoutes = listOf(
        Routes.Location.route,
        Routes.Favorite.route,
        Routes.Home.route,
        Routes.PatientList.route,
        Routes.MyInfo.route
    )

    // MapScreen에서 장소 상세 정보 표시 여부 (헤더/하단바 숨김 제어)
    var showBarsFromMap by remember { mutableStateOf(true) }

    // 통화/인증/알림 화면에서는 헤더/하단바 숨김
    val isCallScreen = baseRoute in callRoutes
    val isAuthScreen = baseRoute in authRoutes
    val isNotificationScreen = baseRoute == Routes.Notifications.route

    val showBottomBar = !isAuthScreen && !isCallScreen && baseRoute in bottomBarRoutes && showBarsFromMap
    val showTopBar = !isAuthScreen && !isCallScreen && !isNotificationScreen && showBarsFromMap

    // SafeZoneSetting 라우트에서 OngilBrandHeaderCard 사용
    val safeZoneSettingRoutes = listOf("safezone_setting")

    OngilThemeProvider(userType = userType) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = Color.Transparent,
            topBar = {
            Box(modifier = Modifier.statusBarsPadding()) {
                if (showTopBar) {
                    // BottomNav 탭 화면 또는 SafeZoneSetting 화면에서는 OngilBrandHeaderCard 사용
                    if (baseRoute in bottomBarRoutes || baseRoute in safeZoneSettingRoutes) {
                        // PatientData를 PatientInfoUi로 변환 (DataStore 우선, API fallback)
                        val patients = patientList.map { patient ->
                            val patientIdStr = patient.id.toString()
                            PatientInfoUi(
                                id = patientIdStr,
                                name = patient.name,
                                profileImageUrl = patientProfileImages[patientIdStr] ?: patient.profileImage
                            )
                        }

                        // 프로필 이미지: DataStore 우선, 없으면 API 데이터 사용
                        val displayProfileImage = if (userType == "GUARDIAN") {
                            // 보호자: 선택된 환자의 프로필 이미지
                            selectedPatientProfileImage
                                ?: patientList.find { it.id.toString() == selectedPatientId }?.profileImage
                        } else {
                            // 환자: 본인의 프로필 이미지
                            currentUserProfileImage
                                ?: currentUserInfo?.getOrNull()?.profileImage
                        }

                        // 프로필 이름: 보호자는 선택된 환자 이름, 환자는 본인 이름
                        val selectedPatientName = if (userType == "GUARDIAN") {
                            patientList.find { it.id.toString() == selectedPatientId }?.name
                        } else {
                            currentUserInfo?.getOrNull()?.name
                        }

                        OngilBrandHeaderCard(
                            onBellClick = {
                                navController.navigate(Routes.Notifications.route) {
                                    launchSingleTop = true
                                }
                            },
                            profileImageUrl = displayProfileImage,
                            profileName = selectedPatientName,
                            patients = patients,
                            onSelectPatient = { patient ->
                                authViewModel.selectPatient(patient.id)
                            },
                            userType = userType,
                            selectedPatientId = selectedPatientId
                        )
                    } else {
                        // 다른 화면에서는 OngilTopBarForRoute 사용
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
                                val userId = currentUserInfo?.getOrNull()?.id?.toLong() ?: 0L
                                "${Routes.Favorite.route}/$userId"
                            } else {
                                route
                            }

                        navController.navigate(navigationRoute) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    userType = userType
                )
            }
        }
    ) { paddingValues ->

//        AppNavGraph(
//            navController = navController,
//            modifier = Modifier.padding(paddingValues).imePadding(),
//            startDestination = if (isLoggedIn == true) Routes.Home.route else Routes.Login.route
//        )

        // *** 여기 건들면 페이지에 따라 화면 패딩 망가지니까 건드리지 말것 (gu) ***
        AppNavGraph(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            paddingValues = paddingValues,
            startDestination = startDestination,
            authViewModel = authViewModel,
            onMapShowBarsChange = { showBars ->
                showBarsFromMap = showBars
            }
        )
        }
    }
}