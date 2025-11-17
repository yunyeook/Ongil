package kr.co.ongil.presentation.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kr.co.ongil.presentation.navigation.Routes


fun NavGraphBuilder.homeGraph(
    navController: NavController,
    paddingValues: PaddingValues,
    authViewModel: kr.co.ongil.presentation.ui.auth.AuthStateViewModel? = null,
    route: String = Routes.Home.route
) {
    composable(route) {
        val viewModel: HomeViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val navigationRoute by viewModel.navigationRoute.collectAsState()
        val navigationProgress by viewModel.navigationProgress.collectAsState()
        val showSafetyZones by viewModel.safetyZoneStateManager.showSafetyZones.collectAsState()

        // AuthStateViewModel에서 사용자 정보 및 환자 위치 가져오기
        val currentUserInfo by (authViewModel?.currentUserInfo ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(initial = null)
        val userType = currentUserInfo?.getOrNull()?.userType ?: ""
        val selectedPatientId by (authViewModel?.selectedPatientId ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(initial = null)
        val patientLocations by (authViewModel?.patientLocations ?: kotlinx.coroutines.flow.flowOf(emptyMap())).collectAsState(initial = emptyMap())

        // 환자 경로 변경 모니터링 및 NavigationRouteManager 동기화 (환자용)
        val patientNavigationRoutes by (authViewModel?.patientNavigationRoutes ?: kotlinx.coroutines.flow.flowOf(emptyMap())).collectAsState(initial = emptyMap())

        androidx.compose.runtime.LaunchedEffect(patientNavigationRoutes, userType, currentUserInfo) {
            // 환자인 경우, SSE로 받은 자신의 경로를 NavigationRouteManager에 동기화
            if (userType == "PATIENT") {
                val myPatientId = currentUserInfo?.getOrNull()?.id?.toLong()
                if (myPatientId != null) {
                    val myRoute = patientNavigationRoutes[myPatientId]
                    android.util.Log.d("HomeGraph", "환자 본인 경로 업데이트: patientId=$myPatientId, route=${myRoute != null}")

                    if (myRoute != null) {
                        // SSE로 받은 경로를 HomeViewModel을 통해 NavigationRouteManager에 저장
                        viewModel.syncNavigationFromSse(myRoute)
                    } else {
                        // 경로가 null이면 길찾기 종료된 것
                        viewModel.clearNavigationFromSse()
                    }
                }
            }
        }

        HomeScreen(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onMapClick = {
                android.util.Log.d("HomeGraph", "🗺️ 지도 클릭 - 위치 탭으로 이동")
                navController.navigate(Routes.Location.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            userType = userType,
            selectedPatientId = selectedPatientId,
            patientLocations = patientLocations,
            locationBus = viewModel.locationBus,
            navigationRoute = navigationRoute,
            navigationProgress = navigationProgress,
            showSafetyZones = showSafetyZones
        )
    }
}