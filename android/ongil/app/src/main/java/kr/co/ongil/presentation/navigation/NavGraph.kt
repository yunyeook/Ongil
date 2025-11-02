package kr.co.ongil.presentation.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kr.co.ongil.presentation.ui.home.HomeScreen
import kr.co.ongil.presentation.ui.favorite.FavoriteScreen
import kr.co.ongil.presentation.ui.placedetail.PlaceDetailScreen
import kr.co.ongil.presentation.ui.placedetail.PlaceDetailViewModel
import kr.co.ongil.data.repository.FavoriteRepository
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kr.co.ongil.presentation.navigation.Screen
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailScreen
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailViewModel

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
                    navController.navigate(Screen.Favorite.route)
                }
            )
        }

        // 즐겨찾기
        composable(Screen.Favorite.route) {
            FavoriteScreen(
                patientId = 1L, // TODO: 실제 선택된 환자 ID로 교체
                onNavigateToPlaceDetail = { favoriteId, placeName, address ->
                    val encodedName = Uri.encode(placeName)
                    val encodedAddress = Uri.encode(address)

                    navController.navigate(
                        Screen.PlaceDetail.createRoute(
                            favoriteId = favoriteId,
                            placeName = encodedName,
                            address = encodedAddress
                        )
                    )
                },
                onNavigateToPatientDetail = { id, name, phone, gender ->
                    val encodedName = Uri.encode(name)
                    val encodedPhone = Uri.encode(phone)
                    val encodedGender = Uri.encode(gender)

                    navController.navigate(
                        Screen.PatientDetail.createRoute(
                            patientId = id,
                            name = encodedName,
                            phoneNumber = encodedPhone,
                            gender = encodedGender
                        )
                    )
                }
            )
        }



        // 환자 상세
        composable(
            route = Screen.PatientDetail.route,
            arguments = listOf(
                navArgument("patientId") { type = NavType.LongType },
                navArgument("name") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType },
                navArgument("gender") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val patientId: Long =
                backStackEntry.arguments?.getLong("patientId") ?: -1L
            val nameArg =
                backStackEntry.arguments?.getString("name")
                    ?.let { Uri.decode(it) } ?: ""
            val phoneArg =
                backStackEntry.arguments?.getString("phoneNumber")
                    ?.let { Uri.decode(it) } ?: ""
            val genderArg =
                backStackEntry.arguments?.getString("gender")
                    ?.let { Uri.decode(it) } ?: ""

            // 싱글톤 Repository 사용 (환자 정보도 동일 저장소에서 관리)
            val repository = remember { FavoriteRepository.getInstance() }

            // Repository에서 실제 환자 데이터 조회
            var actualPatientData by remember { mutableStateOf<kr.co.ongil.presentation.ui.favorite.PatientData?>(null) }

            LaunchedEffect(patientId) {
                actualPatientData = repository.getFavoritePatientDetail(patientId)
            }

            val viewModel = remember(actualPatientData) {
                val patientData = actualPatientData
                if (patientData != null) {
                    PatientDetailViewModel(
                        repository = repository,
                        initialPatientId = patientData.id,
                        initialName = patientData.name,
                        initialPhoneNumber = patientData.phoneNumber,
                        initialGender = patientData.gender
                    )
                } else {
                    PatientDetailViewModel(
                        repository = repository,
                        initialPatientId = patientId,
                        initialName = nameArg,
                        initialPhoneNumber = phoneArg,
                        initialGender = genderArg
                    )
                }
            }

            val uiState by viewModel.uiState.collectAsState()

            PatientDetailScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }


        // 장소 상세
        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(
                navArgument("favoriteId") { type = NavType.LongType },
                navArgument("placeName") { type = NavType.StringType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val favoriteId: Long =
                backStackEntry.arguments?.getLong("favoriteId") ?: 1L
            val placeNameArg =
                backStackEntry.arguments?.getString("placeName")
                    ?.let { Uri.decode(it) } ?: ""
            val addressArg =
                backStackEntry.arguments?.getString("address")
                    ?.let { Uri.decode(it) } ?: ""

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