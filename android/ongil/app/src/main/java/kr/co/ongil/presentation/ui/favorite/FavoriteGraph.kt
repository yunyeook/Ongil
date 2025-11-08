package kr.co.ongil.presentation.ui.favorite



import androidx.navigation.NavGraphBuilder

import androidx.navigation.NavHostController

import androidx.navigation.NavType

import androidx.navigation.compose.composable

import androidx.navigation.compose.navigation

import androidx.navigation.navArgument

import kr.co.ongil.presentation.navigation.Routes



fun NavGraphBuilder.favoriteGraph(navController: NavHostController) {

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

                onGoSearchUserClick = { navController.navigate(Routes.SearchUser.route) }

            )

        }

    }

}
