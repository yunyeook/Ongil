package kr.co.ongil.presentation.ui.Atest

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kr.co.ongil.presentation.ui.favorite.FavoriteScreen
import kr.co.ongil.presentation.ui.placedetail.PlaceDetailScreen
import kr.co.ongil.presentation.ui.placedetail.PlaceDetailViewModel
import kr.co.ongil.data.repository.FavoriteRepository
import kr.co.ongil.presentation.navigation.Screen
import kr.co.ongil.presentation.ui.home.HomeScreen
import android.net.Uri
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailScreen
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.co.ongil.presentation.ui.searchuser.SearchUserScreen
import kr.co.ongil.presentation.ui.searchuser.SearchUserViewModel
import kr.co.ongil.presentation.ui.signup.SignupScreen
import kr.co.ongil.presentation.ui.signup.SignupViewModel


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
                },
                onGoSearchUserClick = {
                    navController.navigate(Screen.SearchUser.route)
                },
                onGoSignupClick = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        // 회원가입
        composable(route = Screen.Signup.route) {
            val viewModel: SignupViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            SignupScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onProfileImageClick = viewModel::onProfileImageClick,
                onNameChange = viewModel::onNameChange,
                onBirthClick = viewModel::onBirthClick,
                onPhoneChange = viewModel::onPhoneChange,
                onRequestVerificationCode = viewModel::onRequestVerificationCode,
                onVerificationCodeChange = viewModel::onVerificationCodeChange,
                onVerifyCodeClick = viewModel::onVerifyCodeClick,
                onPasswordChange = viewModel::onPasswordChange,
                onTogglePasswordVisible = viewModel::onTogglePasswordVisible,
                onPasswordConfirmChange = viewModel::onPasswordConfirmChange,
                onTogglePasswordConfirmVisible = viewModel::onTogglePasswordConfirmVisible,
                onSelectGuardian = viewModel::onSelectGuardian,
                onSelectPatient = viewModel::onSelectPatient,
                onSubmitSignup = viewModel::onSubmitSignup,
                onDismissSuccessModal = viewModel::onDismissSuccessModal,
                onDismissErrorModal = viewModel::onDismissErrorModal,
            )
        }

        // 사용자 찾기 화면
        composable(route = Screen.SearchUser.route) {
            val viewModel: SearchUserViewModel = viewModel()
            SearchUserScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // 즐겨찾기 화면
        composable(route = Screen.Favorite.route) {
            FavoriteScreen(
                patientId = 1L,
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

        // 환자 상세 화면
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

            // 싱글톤 Repository 사용
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
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun PlayGroundMJPreview() {
    PlayGroundMJ()
}