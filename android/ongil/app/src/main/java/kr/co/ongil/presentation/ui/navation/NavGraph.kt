package kr.co.ongil.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kr.co.ongil.presentation.ui.home.HomeScreen
import kr.co.ongil.presentation.ui.favorite.FavoriteScreen
import kr.co.ongil.presentation.ui.favorite.PlaceDetailScreen

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route // ✅ 앱 첫 화면은 홈
    ) {
        // 홈
        composable(Screen.Home.route) {
            HomeScreen(
                onGoFavoriteClick = {
                    // ✅ GreenButton 눌렀을 때 실행될 실제 이동 로직
                    navController.navigate(Screen.Favorite.route)
                }
            )
        }

        // 즐겨찾기
        composable(Screen.Favorite.route) {
            FavoriteScreen(
                onNavigateToPlaceDetail = { placeId ->
                    navController.navigate(
                        Screen.PlaceDetail.createRoute(placeId)
                    )
                }
            )
        }

        // 장소 상세
        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(
                navArgument("placeId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val placeIdArg: Long =
                backStackEntry.arguments?.getLong("placeId") ?: -1L

            PlaceDetailScreen(
                placeLabel = "우리집",
                placeAddress = "서울시 노원구 중계동 ...",
                onBackClick = { navController.popBackStack() },
                onEditNameClick = { /* TODO */ },
                onSetDefaultClick = { /* TODO */ },
                onSaveClick = { /* TODO */ },
                onDeleteClick = { /* TODO */ }
            )
        }
    }
}