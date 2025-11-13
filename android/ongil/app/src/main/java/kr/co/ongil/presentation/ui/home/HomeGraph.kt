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
    route: String = Routes.Home.route
) {
    composable(route) {
        val viewModel: HomeViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()

        HomeScreen(
            uiState = uiState,
            modifier = Modifier.padding(paddingValues),
            onMapClick = {
                navController.navigate(Routes.Location.route)
            }
        )
    }
}