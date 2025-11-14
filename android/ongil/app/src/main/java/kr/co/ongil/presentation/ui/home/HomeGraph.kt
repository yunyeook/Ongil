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

        // AuthStateViewModel에서 사용자 정보 및 환자 위치 가져오기
        val currentUserInfo by (authViewModel?.currentUserInfo ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(initial = null)
        val userType = currentUserInfo?.getOrNull()?.userType ?: ""
        val selectedPatientId by (authViewModel?.selectedPatientId ?: kotlinx.coroutines.flow.flowOf(null)).collectAsState(initial = null)
        val patientLocations by (authViewModel?.patientLocations ?: kotlinx.coroutines.flow.flowOf(emptyMap())).collectAsState(initial = emptyMap())

        HomeScreen(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onMapClick = {
                android.util.Log.d("HomeGraph", "🗺️ 지도 클릭 - 위치 탭으로 이동")
                navController.navigate(Routes.Location.route) {
                    launchSingleTop = true
                }
            },
            userType = userType,
            selectedPatientId = selectedPatientId,
            patientLocations = patientLocations,
            locationBus = viewModel.locationBus
        )
    }
}