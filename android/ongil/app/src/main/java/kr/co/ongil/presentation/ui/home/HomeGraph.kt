package kr.co.ongil.presentation.ui.home

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kr.co.ongil.presentation.navigation.Routes


fun NavGraphBuilder.homeGraph(
    navController: NavController,
    route: String = Routes.Home.route
) {
    composable(route) {
        val viewModel: HomeViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()

        HomeScreen(
            uiState = uiState
        )
    }
}