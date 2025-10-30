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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge() // ✅ super.onCreate() 보다 먼저 호출
        super.onCreate(savedInstanceState)

        setContent {
            OngilTheme {
                // ✅ Column으로 감싸서 겹치지 않게 함
                Column(modifier = Modifier.fillMaxSize()) {
                    // ✅ PlayGroundMJ 실행
                    PlayGroundMJ()
                    // PlayGroundSH() // 지금은 MJ만 확인하기 위해 잠시 주석처리
                }
            }
        }
    }
}