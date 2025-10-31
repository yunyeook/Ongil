package kr.co.ongil.presentation.ui.favorite

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kr.co.ongil.presentation.ui.common.GreenButton

@Composable
fun PlaceList(
    places: List<PlaceItem>,
    onAddPlaceClick: () -> Unit,
    onClickPlaceCard: (Long) -> Unit,
    onClickPlaceIcon: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {

        GreenButton(
            text = "+ 새로운 장소 등록",
            onClick = onAddPlaceClick,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .fillMaxWidth()
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 0.dp,
                bottom = 16.dp
            )
        ) {
            items(places) { place ->
                PlaceCard(
                    label = place.label,
                    address = place.address,
                    onClickCard = { onClickPlaceCard(place.id) },
                    onClickIcon = { onClickPlaceIcon(place.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PlaceCard(
    label: String,
    address: String,
    onClickCard: () -> Unit,
    onClickIcon: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickCard() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = label,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = address,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    lineHeight = 20.sp
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            IconButton(
                onClick = { onClickIcon() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5C7165))
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "위치 보기",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
private fun PlaceListPreview() {
    val dummyPlaces = listOf(
        PlaceItem(
            id = 101L,
            label = "집",
            address = "서울시 서초구"
        ),
        PlaceItem(
            id = 102L,
            label = "병원",
            address = "OO병원 신관 2F 신경과"
        ),
        PlaceItem(
            id = 103L,
            label = "경로당",
            address = "서초구 ○○ 경로당 (매일 13시 방문)"
        )
    )

    Surface(color = Color(0xFFF9FAFB)) {
        PlaceList(
            places = dummyPlaces,
            onAddPlaceClick = {},
            onClickPlaceCard = {},
            onClickPlaceIcon = {}
        )
    }
}