package kr.co.ongil.presentation.ui.Atest

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kr.co.ongil.presentation.ui.favorite.FavoriteScreen
import kr.co.ongil.presentation.ui.favorite.PlaceDetailScreen
import kr.co.ongil.presentation.ui.favorite.PlaceDetailViewModel
import kr.co.ongil.data.repository.FavoriteRepository
import kr.co.ongil.presentation.ui.navigation.Screen
import kr.co.ongil.presentation.ui.home.HomeScreen
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
@Composable
fun PlayGroundMJ() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onGoFavoriteClick = {
                    navController.navigate(Screen.Favorite.route)
                }
            )
        }
        // 즐겨찾기 화면
        composable(route = Screen.Favorite.route) {
            FavoriteScreen(
                patientId = 1L, // TODO: 나중에 선택된 환자 ID로 교체
                onNavigateToPlaceDetail = { favoriteId, placeName, address ->
                    navController.navigate(
                        Screen.PlaceDetail.createRoute( favoriteId, placeName, address)
                    )
                }
            )
        }

        // 장소 상세 화면
        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(
                navArgument("favoriteId") {type = NavType.LongType},
                navArgument("placeName") {type = NavType.StringType},
                navArgument("address") {type = NavType.StringType}
            )
        ) { backStackEntry ->
            val favoriteId = backStackEntry.arguments?.getLong("favoriteId") ?: 1L
            val placeNameArg = backStackEntry.arguments?.getString("placeName") ?: ""
            val addressArg = backStackEntry.arguments?.getString("address") ?: ""

            // 싱글톤 Repository 사용
            val repository = remember { FavoriteRepository.getInstance() }

            // Repository에서 실제 데이터 조회
            var actualPlaceData by remember { mutableStateOf<kr.co.ongil.presentation.ui.favorite.PlaceData?>(null) }

            LaunchedEffect(favoriteId) {
                actualPlaceData = repository.getFavoritePlaceDetail(1L, favoriteId)
            }

            val viewModel = remember(actualPlaceData) {
                val placeData = actualPlaceData
                if (placeData != null) {
                    PlaceDetailViewModel(
                        repository = repository,
                        initialFavoriteId = placeData.favoriteId,
                        initialPlaceName = placeData.placeName,
                        initialAddress = placeData.address,
                        initialIsDefault = placeData.isDefault,
                        initialPatientId = placeData.patientId
                    )
                } else {
                    // 데이터 로딩 중이거나 없을 경우 기본값 사용
                    PlaceDetailViewModel(
                        repository = repository,
                        initialFavoriteId = favoriteId,
                        initialPlaceName = placeNameArg,
                        initialAddress = addressArg,
                        initialIsDefault = false,
                        initialPatientId = 1L
                    )
                }
            }

            val uiState by viewModel.uiState.collectAsState()

            PlaceDetailScreen(
                favoriteId = uiState.favoriteId,
                placeName = uiState.placeName,
                address = uiState.address,
                isDefault = uiState.isDefault,
                onBackClick = { navController.popBackStack() },
                onSetDefaultClick = { viewModel.setAsDefault() },
                onSaveClick = { newName, newAddress ->
                    viewModel.updatePlaceInfo(newName, newAddress)
                    navController.popBackStack()
                },
                onDeleteClick = {
                    viewModel.deletePlace(onSuccess = {
                        navController.popBackStack()
                    })
                }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun PlayGroundMJPreview() {
    PlayGroundMJ()
}