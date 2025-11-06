//package kr.co.ongil.presentation.ui.Atest
//
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.navigation.NavType
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.navigation.navArgument
//import kr.co.ongil.presentation.ui.favorite.FavoriteScreen
//import kr.co.ongil.presentation.ui.placedetail.PlaceDetailScreen
//import kr.co.ongil.presentation.ui.placedetail.PlaceDetailViewModel
//import androidx.hilt.navigation.compose.hiltViewModel
////import kr.co.ongil.data.repository.fake.FakeFavoriteRepository
//import kr.co.ongil.presentation.navigation.Routes
//import kr.co.ongil.presentation.ui.home.HomeScreen
//import android.net.Uri
//import kr.co.ongil.presentation.ui.patientdetail.PatientDetailScreen
//import kr.co.ongil.presentation.ui.patientdetail.PatientDetailViewModel
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.collectAsState
//import androidx.lifecycle.viewmodel.compose.viewModel
//import kr.co.ongil.presentation.ui.searchuser.SearchUserScreen
//import kr.co.ongil.presentation.ui.searchuser.SearchUserViewModel
//import kr.co.ongil.presentation.ui.auth.register.RegisterScreen
//import kr.co.ongil.presentation.ui.auth.register.RegisterViewModel
//
//
//@Composable
//fun PlayGroundMJ() {
//
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = Routes.Home.route
//    ) {
//        composable(route = Routes.Home.route) {
//            HomeScreen(
//                onGoSearchUserClick = {
//                    navController.navigate(Routes.SearchUser.route)
//                },
//                onGoSignupClick = {
//                    navController.navigate(Routes.Register.route)
//                }
//            )
//        }
//
//        // 회원가입
//        composable(route = Routes.Register.route) {
//            val viewModel: RegisterViewModel = hiltViewModel()
//            val uiState by viewModel.uiState.collectAsState()
//
//            RegisterScreen(
//                uiState = uiState,
//                onBackClick = { navController.popBackStack() },
//                onProfileImageClick = viewModel::onProfileImageClick,
//                onNameChange = viewModel::onNameChange,
//                onBirthClick = viewModel::onBirthClick,
//                onSetBirth = { value -> viewModel.onSetBirth(value ?: "") },
//                onDismissDatePicker = viewModel::onDismissDatePicker,
//                onPhoneChange = viewModel::onPhoneChange,
//                onRequestVerificationCode = viewModel::onRequestVerificationCode,
//                onVerificationTokenChange = viewModel::onVerificationCodeChange,
//                onVerifyTokenClick = viewModel::onVerifyTokenClick,
//                onPasswordChange = viewModel::onPasswordChange,
//                onTogglePasswordVisible = viewModel::onTogglePasswordVisible,
//                onPasswordConfirmChange = viewModel::onPasswordConfirmChange,
//                onTogglePasswordConfirmVisible = viewModel::onTogglePasswordConfirmVisible,
//                onSelectGuardian = viewModel::onSelectGuardian,
//                onSelectPatient = viewModel::onSelectPatient,
//                onSubmitRegister = viewModel::onSubmitRegister,
//                onDismissSuccessModal = viewModel::onDismissSuccessModal,
//                onDismissErrorModal = viewModel::onDismissErrorModal,
//            )
//        }
//
//        // 사용자 찾기 화면
//        composable(route = Routes.SearchUser.route) {
//            val viewModel: SearchUserViewModel = viewModel()
//            SearchUserScreen(
//                navController = navController,
//                viewModel = viewModel
//            )
//        }
//
//        // 즐겨찾기 화면
//        composable(
//            route = "${Routes.Favorite.route}/{patientId}",
//            arguments = listOf(
//                androidx.navigation.navArgument("patientId") { type = androidx.navigation.NavType.LongType }
//            )
//        ) {
//            FavoriteScreen(
//                onNavigateToPlaceDetail = { patientId, favoriteId ->
//                    navController.navigate(
//                        Routes.PlaceDetail.createRoute(
//                            patientId = patientId,
//                            favoriteId = favoriteId
//                        )
//                    )
//                },
//                onNavigateToPatientDetail = { id, name, phone ->
//                    val encodedName = Uri.encode(name)
//                    val encodedPhone = Uri.encode(phone)
//
//                    navController.navigate(
//                        Routes.PatientDetail.createRoute(
//                            patientId = id,
//                            name = encodedName,
//                            phoneNumber = encodedPhone
//                        )
//                    )
//                },
//                onGoSearchUserClick = {
//                    navController.navigate(Routes.SearchUser.route)
//                },
//
//            )
//        }
//
//        // 장소 상세 화면
//        composable(
//            route = Routes.PlaceDetail.route,
//            arguments = Routes.PlaceDetail.arguments
//        ) {
//            val viewModel: PlaceDetailViewModel = hiltViewModel()
//            PlaceDetailScreen(
//                viewModel = viewModel,
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        // 환자 상세 화면
//        composable(
//            route = Routes.PatientDetail.route,
//            arguments = listOf(
//                navArgument("patientId") { type = NavType.LongType },
//                navArgument("name") { type = NavType.StringType },
//                navArgument("phoneNumber") { type = NavType.StringType },
//                navArgument("gender") { type = NavType.StringType }
//            )
//        ) {
//            val viewModel: PatientDetailViewModel = hiltViewModel()
//            PatientDetailScreen(
//                viewModel = viewModel,
//                onNavigateBack = { navController.popBackStack() }
//            )
//        }
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
//@Composable
//private fun PlayGroundMJPreview() {
//    PlayGroundMJ()
//}