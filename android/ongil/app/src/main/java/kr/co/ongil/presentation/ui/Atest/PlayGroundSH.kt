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
