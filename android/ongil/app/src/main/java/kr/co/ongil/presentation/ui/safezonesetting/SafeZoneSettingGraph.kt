package kr.co.ongil.presentation.ui.safezonesetting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kr.co.ongil.presentation.ui.auth.AuthStateViewModel

/**
 * 안전구역 설정 NavGraph
 */
fun NavGraphBuilder.safeZoneGraph(
    navController: NavHostController,
    paddingValues: PaddingValues = PaddingValues(),
    authViewModel: AuthStateViewModel
) {
    navigation(
        route = SafeZoneSettingRoutes.GRAPH,
        startDestination = SafeZoneSettingRoutes.SETTING
    ) {
        composable(SafeZoneSettingRoutes.SETTING) {
            val viewModel: SafeZoneSettingViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsState()

            // 사용자 타입 확인
            val currentUserInfo by authViewModel.currentUserInfo.collectAsState(initial = null)
            val userType = currentUserInfo?.getOrNull()?.userType ?: ""

            // 보호자만 접근 가능
            if (userType.uppercase() == "GUARDIAN") {
                SafeZoneSettingScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onBack = { navController.navigateUp() },
                    modifier = Modifier.padding(paddingValues)
                )
            } else {
                // 환자는 접근 불가
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("보호자만 접근할 수 있는 페이지입니다.")
                }
            }
        }
    }
}
