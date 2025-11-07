package kr.co.ongil.presentation.ui.favorite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FavoriteTabBar(
    selectedTab: FavoriteTab,
    onTabSelected: (FavoriteTab) -> Unit,
    modifier: Modifier = Modifier,
    userType: String = ""
) {
    // userType에 따라 탭 이름 결정
    // PATIENT(환자)가 로그인한 경우: '보호자 목록'
    // GUARDIAN(보호자)가 로그인한 경우: '환자 목록'
    val patientsTabText = when (userType.uppercase()) {
        "PATIENT" -> "보호자 목록"
        "GUARDIAN" -> "환자 목록"
        else -> "환자 목록" // 기본값
    }

    Surface(
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // 환자/보호자 목록 탭
            FavoriteTabChip(
                text = patientsTabText,
                isSelected = (selectedTab == FavoriteTab.PATIENTS),
                onClick = { onTabSelected(FavoriteTab.PATIENTS) },
                modifier = Modifier
                    .weight(1f)
            )

            // 장소 목록 탭
            FavoriteTabChip(
                text = "장소 목록",
                isSelected = (selectedTab == FavoriteTab.PLACES),
                onClick = { onTabSelected(FavoriteTab.PLACES) },
                modifier = Modifier
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun FavoriteTabChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) Color.White else Color.Transparent
    val textColor = if (isSelected) Color(0xFF111827) else Color(0xFF6B7280)
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun FavoriteTabBarPreviewPatients() {
    FavoriteTabBar(
        selectedTab = FavoriteTab.PATIENTS,
        onTabSelected = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun FavoriteTabBarPreviewPlaces() {
    FavoriteTabBar(
        selectedTab = FavoriteTab.PLACES,
        onTabSelected = {}
    )
}