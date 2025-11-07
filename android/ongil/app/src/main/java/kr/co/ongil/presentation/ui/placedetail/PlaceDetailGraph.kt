package kr.co.ongil.presentation.ui.placedetail

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.ongil.presentation.navigation.Routes

fun NavGraphBuilder.placeDetailGraph(navController: NavHostController) {
    composable(
        route = Routes.PlaceDetail.route,
        arguments = Routes.PlaceDetail.arguments
    ) { backStackEntry ->
        val viewModel: PlaceDetailViewModel = hiltViewModel()
        PlaceDetailScreen(
            viewModel = viewModel,
            onBackClick = { navController.popBackStack() },
            onSavedSuccess = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("favorite_updated", true)
                navController.popBackStack()
            },
            onDeletedSuccess = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set("favorite_updated", true)
                navController.popBackStack()
            }
        )
    }
}