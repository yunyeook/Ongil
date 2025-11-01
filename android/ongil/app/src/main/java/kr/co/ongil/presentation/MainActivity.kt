package kr.co.ongil.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import kr.co.ongil.presentation.theme.OngilTheme
import kr.co.ongil.presentation.ui.Atest.PlayGroundMJ
import kr.co.ongil.presentation.ui.Atest.PlayGroundSH


// 나중에 커밋할때는 플레이그라운드 다 주석처리해주세요
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // ✅ super.onCreate() 보다 먼저 호출
        super.onCreate(savedInstanceState)

        setContent {
            OngilTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 혹시 충돌 날 수도 있으니까 커밋이나 푸시할때는 여기 밑에 전부 주석처리 해주세요
//                    PlayGroundMJ()
                    // PlayGroundSH()
                }
            }
        }
    }
}