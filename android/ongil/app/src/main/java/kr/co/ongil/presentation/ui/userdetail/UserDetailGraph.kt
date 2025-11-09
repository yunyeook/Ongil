package kr.co.ongil.presentation.ui.userdetail

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

fun NavGraphBuilder.userDetailGraph(navController: NavHostController) {
    composable(
        route = UserDetailRoutes.Detail.route,
        arguments = UserDetailRoutes.Detail.arguments
    ) { backStackEntry ->
        val viewModel: UserDetailViewModel = hiltViewModel()
        UserDetailScreen(
            viewModel = viewModel,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
