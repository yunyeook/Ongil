package kr.co.ongil.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kr.co.ongil.presentation.ui.myinfo.MyInfoEditScreen
import kr.co.ongil.presentation.ui.myinfo.MyInfoScreen
import kr.co.ongil.presentation.viewmodel.MyInfoViewModel

/**
 * 앱 Navigation Graph
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.MyInfo.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
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
            val viewModel: MyInfoViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            MyInfoEditScreen(
                roleLabel = "보호자", // TODO: 실제 사용자 역할
                nameInit = uiState.name,
                birthInit = "1990.01.01", // TODO: 생년월일 추가
                phoneInit = uiState.phoneNumber,
                profileImageUrl = uiState.profileImage,
                onPickProfileImage = {
                    // TODO: 이미지 선택 로직
                },
                onBirthPickClick = {
                    // TODO: 날짜 선택 로직
                },
                onSendVerifyCode = { newPhone ->
                    // TODO: 인증번호 발송 API
                },
                onResendCode = { newPhone ->
                    // TODO: 인증번호 재발송 API
                },
                onVerifyCode = { newPhone, code ->
                    // TODO: 인증번호 확인 API
                },
                onChangePasswordClick = {
                    // TODO: 비밀번호 변경 화면으로 이동
                },
                onSaveClick = { name, birth, phone ->
                    // TODO: 정보 수정 API
                    navController.popBackStack()
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