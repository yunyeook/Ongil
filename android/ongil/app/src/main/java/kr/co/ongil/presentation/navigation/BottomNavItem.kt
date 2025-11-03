package kr.co.ongil.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 하단 네비게이션 바 아이템 정의
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * 온길 앱의 하단 네비게이션 아이템 목록
 */
object OngilBottomNavItems {
    val location = BottomNavItem(
        route = Routes.Location.route,
        label = "위치",
        icon = Icons.Filled.LocationOn
    )

    val favorite = BottomNavItem(
        route = Routes.Favorite.route,
        label = "즐겨찾기",
        icon = Icons.Filled.Favorite
    )

    val home = BottomNavItem(
        route = Routes.Home.route,
        label = "홈",
        icon = Icons.Filled.Home
    )

    val patientList = BottomNavItem(
        route = Routes.PatientList.route,
        label = "환자 정보",
        icon = Icons.Filled.People
    )

    val myInfo = BottomNavItem(
        route = Routes.MyInfo.route,
        label = "나의 정보",
        icon = Icons.Filled.Person
    )

    /**
     * 모든 하단 네비게이션 아이템 리스트 (순서대로)
     */
    val all = listOf(location, favorite, home, patientList, myInfo)
}
