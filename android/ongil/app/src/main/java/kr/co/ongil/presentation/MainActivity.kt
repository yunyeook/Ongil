package kr.co.ongil.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import kr.co.ongil.presentation.navigation.MainScreen
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.Atest.PlayGroundGK
//import kr.co.ongil.presentation.ui.Atest.PlayGroundMJ
import kr.co.ongil.presentation.ui.Atest.PlayGroundSH
import kr.co.ongil.presentation.ui.call.VoipCallDebugScreen


// 나중에 커밋할때는 플레이그라운드 다 주석처리해주세요
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OngilTheme {
//                VoipCallDebugScreen()
                 MainScreen()
//                PlayGroundMJ()
                // PlayGroundSH()
                // PlayGroundGK()
            }
        }
    }
}