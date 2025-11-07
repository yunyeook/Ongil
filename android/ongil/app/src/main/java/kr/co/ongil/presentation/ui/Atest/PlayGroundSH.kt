package kr.co.ongil.presentation.ui.Atest

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.myinfo.MyInfoEditScreen
import kr.co.ongil.presentation.ui.myinfo.MyInfoScreen
import kr.co.ongil.presentation.ui.myinfo.MyInfoUiState
import kr.co.ongil.presentation.uistate.MyInfoEditUiState
import kr.co.ongil.presentation.viewmodel.MyInfoEditViewModel
import kr.co.ongil.presentation.navigation.AppNavGraph
import kr.co.ongil.presentation.ui.common.OngilTopBarForRoute

@Composable
fun PlayGroundSH(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AppNavGraph(navController = navController)
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "PlayGroundSH Preview"
)
@Composable
private fun PlayGroundSHPreview() {
    OngilTheme {
        PlayGroundSH()
    }
}

// ---- TopBar 테스트 Preview ----
@Preview(showBackground = true, name = "TopBar - 내 정보")
@Composable
private fun TopBarMyInfoPreview() {
    OngilTheme {
        OngilTopBarForRoute(
            route = "my_info",
            onBackClick = {},
            onBellClick = {}
        )
    }
}

@Preview(showBackground = true, name = "TopBar - 내 정보 수정")
@Composable
private fun TopBarMyInfoEditPreview() {
    OngilTheme {
        OngilTopBarForRoute(
            route = "my_info_edit",
            onBackClick = {},
            onBellClick = {}
        )
    }
}

@Preview(showBackground = true, name = "TopBar - 최근 통화 목록")
@Composable
private fun TopBarRecentCallsPreview() {
    OngilTheme {
        OngilTopBarForRoute(
            route = "recent_calls",
            onBackClick = {},
            onBellClick = {}
        )
    }
}

@Preview(showBackground = true, name = "TopBar - 비밀번호 변경")
@Composable
private fun TopBarChangePasswordPreview() {
    OngilTheme {
        OngilTopBarForRoute(
            route = "change_password",
            onBackClick = {},
            onBellClick = {}
        )
    }
}

@Preview(showBackground = true, name = "TopBar - 사용자 찾기")
@Composable
private fun TopBarSearchUserPreview() {
    OngilTheme {
        OngilTopBarForRoute(
            route = "search_user",
            onBackClick = {},
            onBellClick = {}
        )
    }
}

@Preview(showBackground = true, name = "TopBar - 사용자 등록")
@Composable
private fun TopBarRegisterUserPreview() {
    OngilTheme {
        OngilTopBarForRoute(
            route = "register_user",
            onBackClick = {},
            onBellClick = {}
        )
    }
}

@Preview(showBackground = true, name = "TopBar - 환자 상세")
@Composable
private fun TopBarPatientDetailPreview() {
    OngilTheme {
        OngilTopBarForRoute(
            route = "patient_detail",
            onBackClick = {},
            onBellClick = {}
        )
    }
}

@Preview(showBackground = true, name = "TopBar - 장소 상세")
@Composable
private fun TopBarPlaceDetailPreview() {
    OngilTheme {
        OngilTopBarForRoute(
            route = "place_detail",
            onBackClick = {},
            onBellClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "All TopBars")
@Composable
private fun AllTopBarsPreview() {
    OngilTheme {
        Column {
            val routes = listOf(
                "my_info",
                "my_info_edit",
                "recent_calls",
                "change_password",
                "search_user",
                "register_user",
                "patient_detail",
                "place_detail"
            )
            routes.forEach { route ->
                OngilTopBarForRoute(
                    route = route,
                    onBackClick = {},
                    onBellClick = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
