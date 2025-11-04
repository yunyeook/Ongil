package kr.co.ongil.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.net.Uri
import androidx.compose.runtime.remember
import kr.co.ongil.data.repository.FakeFavoriteRepository
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import kr.co.ongil.presentation.ui.myinfo.MyInfoEditScreen
import kr.co.ongil.presentation.ui.myinfo.MyInfoScreen
import kr.co.ongil.presentation.ui.signup.SignupScreen
import kr.co.ongil.presentation.ui.signup.SignupViewModel
import kr.co.ongil.presentation.ui.myinfo.ChangePasswordScreen
import kr.co.ongil.presentation.ui.myinfo.RecentCallsScreen
import kr.co.ongil.presentation.ui.myinfo.CallDetailScreen
import kr.co.ongil.presentation.ui.notification.NotificationScreen
import kr.co.ongil.presentation.viewmodel.MyInfoViewModel
import kr.co.ongil.presentation.ui.favorite.FavoriteScreen
import kr.co.ongil.presentation.ui.searchuser.SearchUserScreen
import kr.co.ongil.presentation.ui.placedetail.PlaceDetailScreen
import kr.co.ongil.presentation.ui.placedetail.PlaceDetailViewModel
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailViewModel
import kr.co.ongil.presentation.ui.patientdetail.PatientDetailScreen
import kr.co.ongil.presentation.ui.home.HomeScreen
import androidx.navigation.NavController
import kr.co.ongil.presentation.ui.searchuser.SearchUserViewModel
/**
 * 앱 Navigation Graph
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Routes.Home.route
) {
    NavHost(
        navController = navController, startDestination = startDestination, modifier = modifier
    ) {
        // 홈 화면
        composable(Routes.Home.route) {
            HomeScreen(
                onGoSearchUserClick = { navController.navigate(Routes.SearchUser.route) },
                onGoSignupClick = { navController.navigate(Routes.Signup.route) }
            )
        }
        // 위치 화면
        composable(Routes.Location.route) {
            PlaceholderScreen("위치")
        }

        // 즐겨찾기 화면
        composable(Routes.Favorite.route) {
            FavoriteScreen(
                patientId = 1L,
                onNavigateToPlaceDetail = { favoriteId, placeName, address ->
                    navController.navigate(
                        Routes.PlaceDetail.createRoute(
                            favoriteId, placeName, address
                        )
                    )
                },
                onNavigateToPatientDetail = { id, name, phone, gender ->
                    navController.navigate(
                        Routes.PatientDetail.createRoute(
                            id, name, phone, gender
                        )
                    )
                },
                onGoSearchUserClick = { navController.navigate(Routes.SearchUser.route) }
            )
        }

        // 장소 상세
        composable(
            route = Routes.PlaceDetail.route, arguments = Routes.PlaceDetail.arguments
        ) { backStack ->
            val favoriteId = backStack.arguments?.getLong("favoriteId") ?: 1L
            val placeNameArg = backStack.arguments?.getString("placeName").orEmpty()
            val addressArg = backStack.arguments?.getString("address").orEmpty()

            // 디코딩(표시용)
            val placeName = Uri.decode(placeNameArg)
            val address = Uri.decode(addressArg)

            // 더미 저장소 & 뷰모델 인스턴스 (Hilt 적용 전 임시 방식)
            val repository = remember { FakeFavoriteRepository.getInstance() }
            val viewModel = remember(favoriteId, placeName, address) {
                PlaceDetailViewModel(
                    repository = repository,
                    initialFavoriteId = favoriteId,
                    initialPlaceName = placeName,
                    initialAddress = address,
                    initialIsDefault = false, // TODO: 실제 값 연동 시 교체
                    initialPatientId = 1L     // TODO: 실제 값 연동 시 교체
                )
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
                },
                onDeleteClick = {
                    viewModel.deletePlace(onSuccess = { navController.popBackStack() })
                })
        }

        // 환자 상세
        composable(
            route = Routes.PatientDetail.route, arguments = Routes.PatientDetail.arguments
        ) { backStack ->
            val patientId = backStack.arguments?.getLong("patientId") ?: -1L
            val nameArg = backStack.arguments?.getString("name").orEmpty()
            val phoneArg = backStack.arguments?.getString("phoneNumber").orEmpty()
            val genderArg = backStack.arguments?.getString("gender").orEmpty()

            val name = Uri.decode(nameArg)
            val phone = Uri.decode(phoneArg)
            val gender = Uri.decode(genderArg)

            val repository = remember { FakeFavoriteRepository.getInstance() }
            val viewModel = remember(patientId, name, phone, gender) {
                PatientDetailViewModel(
                    repository = repository,
                    initialPatientId = patientId,
                    initialName = name,
                    initialPhoneNumber = phone,
                    initialGender = gender
                )
            }

            PatientDetailScreen(
                viewModel = viewModel, onNavigateBack = { navController.popBackStack() })
        }


        // 환자 정보 화면
        composable(Routes.PatientList.route) {
            PlaceholderScreen("환자 정보")
        }

        // 사용자 찾기 화면 (현재 임시)
        composable(Routes.SearchUser.route) {
            // ✅ 플레이그라운드와 동일하게 여기서 뷰모델을 생성
            val vm: SearchUserViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            SearchUserScreen(
                navController = navController,
                viewModel = vm
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
            val editViewModel: kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel = viewModel()

            MyInfoEditScreen(
                viewModel = editViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onChangePasswordClick = {
                    navController.navigate(Routes.ChangePassword.route)
                }
            )
        }

        // 최근 통화목록 화면
        composable(Routes.CallHistory.route) {
            RecentCallsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCallDetail = { callId ->
                    navController.navigate(Routes.CallDetail.createRoute(callId))
                }
            )
        }

        // 비밀번호 변경 화면
        composable(Routes.ChangePassword.route) {
            ChangePasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPasswordChanged = {
                    // 비밀번호 변경 성공 시 추가 동작이 필요하면 여기에 구현
                }
            )
        }

        // 통화 상세 화면
        composable(
            route = Routes.CallDetail.route, arguments = listOf(
                navArgument("callLogId") { type = NavType.LongType })
        ) { backStackEntry ->
            val callLogId = backStackEntry.arguments?.getLong("callLogId") ?: 0L

            CallDetailScreen(
                callLogId = callLogId
            )
        }

        // 회원가입 화면
        composable(Routes.Signup.route) {
            val viewModel: SignupViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            SignupScreen(
                uiState = uiState,
                onBackClick = { navController.popBackStack() },
                onProfileImageClick = viewModel::onProfileImageClick,
                onNameChange = viewModel::onNameChange,
                onBirthClick = viewModel::onBirthClick,
                onSetBirth = viewModel::onSetBirth,
                onDismissDatePicker = viewModel::onDismissDatePicker,
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
    }
}


/**
 * 임시 화면 (실제 화면 구현 전까지 사용)
 */
@Composable
private fun PlaceholderScreen(title: String) {
    Surface(
        modifier = Modifier.fillMaxSize(), color = Color.White
    ) {
        Box(
            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$title 화면\n(구현 예정)",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
    }
}