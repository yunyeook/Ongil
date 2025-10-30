package kr.co.ongil.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.Atest.PlayGroundMJ
import kr.co.ongil.presentation.ui.Atest.PlayGroundSH

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OngilTheme {
                // ✅ PlayGroundMJ 실행
                PlayGroundMJ()
                PlayGroundSH()
            }
        }
    }
}