package kr.co.ongil.presentation.ui.common.bottomnav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/** 하단바 탭 구성 */
object OngilBottomNavItems {
    val Map     = BottomNavItem(route = "map",        label = "지도",     icon = Icons.Outlined.LocationOn)
    val Fav     = BottomNavItem(route = "favorite",   label = "즐겨찾기", icon = Icons.Outlined.Star)
    val Home    = BottomNavItem(route = "home",       label = "홈",       icon = Icons.Outlined.Home)
    val Patient = BottomNavItem(route = "patient",    label = "환자 정보", icon = Icons.Outlined.Assignment)
    val MyInfo  = BottomNavItem(route = "my_info",    label = "나의 정보", icon = Icons.Outlined.AccountCircle)

    val all = listOf(Map, Fav, Home, Patient, MyInfo)
}
