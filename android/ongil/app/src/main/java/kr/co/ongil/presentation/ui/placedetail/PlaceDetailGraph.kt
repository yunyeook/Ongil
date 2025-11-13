package kr.co.ongil.presentation.ui.placedetail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kr.co.ongil.presentation.navigation.Routes

fun NavGraphBuilder.placeDetailGraph(
    navController: NavHostController,
    paddingValues: PaddingValues = PaddingValues()
) {
    composable(
        route = Routes.PlaceDetail.route,
        arguments = Routes.PlaceDetail.arguments
    ) { backStackEntry ->
        val viewModel: PlaceDetailViewModel = hiltViewModel()
        PlaceDetailScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(paddingValues),
            onBackClick = { navController.popBackStack() },
            onSavedSuccess = {
                val currentState = viewModel.uiState.value
                android.util.Log.d("PlaceDetailGraph", "저장 성공 - favoriteId=${currentState.favoriteId}, placeName=${currentState.placeName}, isDefault=${currentState.isDefault}")
                navController.previousBackStackEntry
                    ?.savedStateHandle?.apply {
                        set("favorite_updated", true)
                        set("updated_favorite_id", currentState.favoriteId)
                        set("updated_place_alias", currentState.placeName)
                        set("updated_is_default", currentState.isDefault)
                        android.util.Log.d("PlaceDetailGraph", "savedState 설정 완료")
                    }
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