package kr.co.ongil.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 온길 앱의 테마 색상을 제공하는 데이터 클래스
 */
data class OngilColors(
    val accent: Color,              // 주요 액센트 색상 (버튼, 선택된 아이콘 등)
    val iconSelected: Color,        // 선택된 아이콘 색상
    val iconUnselected: Color,      // 선택되지 않은 아이콘 색상
    val background: Color,          // 배경 색상 (베이지 등)
)

/**
 * Guardian(보호자)용 색상 테마
 */
val GuardianColors = OngilColors(
    accent = OngilAccent,
    iconSelected = OngilIconSelected,
    iconUnselected = OngilIconUnselected,
    background = OngilBeige
)

/**
 * Patient(환자)용 색상 테마
 */
val PatientColors = OngilColors(
    accent = OngilAccentPatient,
    iconSelected = OngilIconSelectedPatient,
    iconUnselected = OngilIconUnselectedPatient,
    background = OngilBeigePatient
)

/**
 * CompositionLocal for providing theme colors
 */
val LocalOngilColors = staticCompositionLocalOf { GuardianColors }

/**
 * userType에 따라 적절한 테마를 제공하는 Provider
 */
@Composable
fun OngilThemeProvider(
    userType: String,
    content: @Composable () -> Unit
) {
    val colors = if (userType.uppercase() == "PATIENT") {
        PatientColors
    } else {
        GuardianColors
    }

    CompositionLocalProvider(LocalOngilColors provides colors) {
        content()
    }
}

/**
 * 현재 테마 색상을 가져오는 헬퍼 프로퍼티
 */
val ongilColors: OngilColors
    @Composable
    get() = LocalOngilColors.current
