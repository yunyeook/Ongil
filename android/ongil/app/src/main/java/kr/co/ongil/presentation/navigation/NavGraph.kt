package kr.co.ongil.presentation.navigation

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
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailScreen
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailViewModel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.co.ongil.presentation.ui.myinfo.MyInfoEditScreen
import kr.co.ongil.presentation.ui.myinfo.MyInfoScreen
import kr.co.ongil.presentation.viewmodel.MyInfoViewModel




@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.MyInfo.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 홈
        composable(Screen.Home.route) {
            HomeScreen(
                onGoFavoriteClick = {
                    navController.navigate(Screen.Favorite.route)
                },
                onGoMyInfoClick = {
                    navController.navigate(Routes.MyInfo.route)
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

        // 나의 정보 화면
        composable(Routes.MyInfo.route) {
            val viewModel: MyInfoViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            MyInfoScreen(
                uiState = uiState,
                onEditInfo = {
                    navController.navigate(Routes.EditInfo.route)
                },
                onRecentCalls = {
                    navController.navigate(Routes.CallHistory.route)
                },
                onLogout = {
                    viewModel.logout()
                    // TODO: 로그아웃 성공시 로그인 화면으로 이동
                    // navController.navigate(Routes.Login.route) {
                    //     popUpTo(Routes.MyInfo.route) { inclusive = true }
                    // }
                }
            )
        }

        // 내 정보 수정 화면
        composable(Routes.EditInfo.route) {
            val myInfoViewModel: MyInfoViewModel = viewModel()
            val myInfoUiState by myInfoViewModel.uiState.collectAsState()

            val editViewModel: kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel = viewModel(
                factory = androidx.lifecycle.viewmodel.viewModelFactory {
                    addInitializer(kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel::class) {
                        kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel(
                            initialName = myInfoUiState.name,
                            initialBirth = "1990.01.01", // TODO: 생년월일 추가
                            initialPhone = myInfoUiState.phoneNumber,
                            initialProfileImageUrl = myInfoUiState.profileImage,
                            initialRoleLabel = "보호자" // TODO: 실제 사용자 역할
                        )
                    }
                }
            )

            MyInfoEditScreen(
                viewModel = editViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onChangePasswordClick = {
                    // TODO: 비밀번호 변경 화면으로 이동
                }
            )
        }

        // 최근 통화목록 화면
        composable(Routes.CallHistory.route) {
            // TODO: CallHistoryScreen 구현
            PlaceholderScreen(title = "최근 통화목록")
        }
    }
}

/**
 * 임시 화면 (실제 화면 구현 전까지 사용)
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title 화면\n(구현 예정)",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}