package kr.co.ongil.presentation.ui.userdetail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

fun NavGraphBuilder.userDetailGraph(
    navController: NavHostController,
    paddingValues: PaddingValues = PaddingValues()
) {
    composable(
        route = UserDetailRoutes.Detail.route,
        arguments = UserDetailRoutes.Detail.arguments
    ) { backStackEntry ->
        val viewModel: UserDetailViewModel = hiltViewModel()
        UserDetailScreen(
            viewModel = viewModel,
            userType = viewModel.uiState.value.userType,
            modifier = Modifier.padding(paddingValues),
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
