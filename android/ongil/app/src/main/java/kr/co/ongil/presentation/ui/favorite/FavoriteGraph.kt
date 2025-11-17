package kr.co.ongil.presentation.ui.favorite

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import kr.co.ongil.presentation.navigation.Routes

fun NavGraphBuilder.favoriteGraph(
    navController: NavHostController,
    paddingValues: PaddingValues = PaddingValues()
) {
    navigation(
        route = "favorite_graph",
        startDestination = "${Routes.Favorite.route}/{patientId}"
    ) {
        composable(
            route = "${Routes.Favorite.route}/{patientId}",
            arguments = listOf(
                navArgument("patientId") { type = NavType.LongType }
            )
        ) {
            FavoriteScreen(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                onNavigateToPlaceDetail = { patientId, favoriteId ->
                    navController.navigate(
                        Routes.PlaceDetail.createRoute(
                            patientId = patientId,
                            favoriteId = favoriteId
                        )
                    )
                },
                onNavigateToPatientDetail = { relationshipId ->
                    navController.navigate(
                        kr.co.ongil.presentation.ui.userdetail.UserDetailRoutes.Detail.createRoute(
                            relationshipId = relationshipId
                        )
                    )
                },
                onGoSearchUserClick = { navController.navigate(Routes.SearchUser.route) },
                onGoSearchPlaceClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("search_placeholder", "즐겨찾기에 등록할 장소를 검색해주세요.")
                    navController.currentBackStackEntry?.savedStateHandle?.set("request_search_focus", true)
                    navController.navigate(Routes.Location.route)
                },
                onNavigateToCall = { targetName, targetPhone, targetId, userType ->
                    // VoipCall 화면으로 이동
                    navController.navigate(
                        Routes.VoipCall.createRoute(
                            targetName = targetName,
                            targetPhone = targetPhone,
                            isCaller = true,
                            userType = userType, // 현재 사용자 타입
                            callId = 0L, // 일반 전화는 callId 없음
                            receiverId = targetId
                        )
                    )
                }
            )
        }
    }
}
