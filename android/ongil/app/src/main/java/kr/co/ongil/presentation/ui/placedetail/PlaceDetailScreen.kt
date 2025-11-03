package kr.co.ongil.presentation.ui.placedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.InputBox


@Composable
fun PlaceDetailScreen(
    favoriteId: Long,
    placeName: String,
    address: String,
    isDefault: Boolean,
    onBackClick: () -> Unit,
    onSetDefaultClick: () -> Unit,
    onSaveClick: (String, String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedPlaceName by remember { mutableStateOf(placeName) }
    var editedAddress by remember { mutableStateOf(address) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF9FAFB) // 배경 연한 그레이 톤 (즐겨찾기 화면과 동일)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

            // ===== 상단 헤더 영역 =====
            TopBarPlaceDetail(
                title = "장소 상세",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(24.dp))

            InputBox(
                label = "장소명",
                placeholder = "",
                value = editedPlaceName,
                onValueChange = {
                    editedPlaceName = it
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            InputBox(
                label = "주소",
                placeholder = "",
                value = editedAddress,
                onValueChange = {
//                    근데 주소도 수정할 수 있어요?
                    editedAddress = it
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ===== 버튼 영역 =====
            GreenButton(
                text = "기본 목적지로 설정",
                onClick = {
                    onSetDefaultClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                containerColor = if (isDefault) Color(0xFF87A293) else Color(0xFFD1D5DB)
            )

            GreenButton(
                text = "저장하기",
                onClick = { onSaveClick(editedPlaceName, editedAddress) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            GreenButton(
                text = "삭제하기",
                onClick = onDeleteClick,
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // ===== 하단 탭바 영역 (Bottom Navigation) =====

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}


@Composable
private fun TopBarPlaceDetail(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp) // 상태바 아래 여백 느낌
            .heightIn(min = 40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 뒤로가기 + 타이틀
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color(0xFF111827)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111827)
            )
        }

        // 우측 프로필/알림 - 아직 더미
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 프로필 아바타 자리 (지금은 동그란 컬러 박스로 대체)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E7FF))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "알림",
                tint = Color(0xFF4B5563),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFFE5E7EB)
@Composable
private fun PlaceDetailScreenPreview() {
    PlaceDetailScreen(
        favoriteId = 1L,
        placeName = "뭐라고할까요",
        address = "주소가어디게",
        isDefault = false,
        onBackClick = {},
        onSetDefaultClick = {},
        onSaveClick = { _, _ -> },
        onDeleteClick = {}
    )
}