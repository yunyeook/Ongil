package kr.co.ongil.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import kr.co.ongil.presentation.navigation.MainScreen
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
                // 하단바 포함된 메인 화면
                MainScreen()

//                // 테스트용 플레이그라운드 (필요시 주석 해제)
//                Column(modifier = Modifier.fillMaxSize()) {
//                    PlayGroundMJ()
 //                   PlayGroundSH()
//                }
            }
        }
    }
}