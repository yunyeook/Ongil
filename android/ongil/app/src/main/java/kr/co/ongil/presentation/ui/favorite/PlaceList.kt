package kr.co.ongil.presentation.ui.favorite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.ongil.domain.model.FavoritePlace
import kr.co.ongil.presentation.ui.common.GreenButton
import kr.co.ongil.presentation.ui.common.favorite.PlaceCard

@Composable
fun PlaceList(
    places: List<FavoritePlace>,
    onAddPlaceClick: () -> Unit,
    onClickPlaceIcon: (Long) -> Unit,
    onClickPlaceCardWithPatient: (patientId: Long, favoriteId: Long) -> Unit,
    onGoSearchPlaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        GreenButton(
            text = "+ 새로운 장소 등록",
            onClick = onGoSearchPlaceClick,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 18.dp)
                .fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(places) { place ->
                PlaceCard(
                    name = place.displayName,
                    address = place.address,
                    isDefault = place.isDefault,
                    onClickCard = { onClickPlaceCardWithPatient(place.patientId, place.favoriteId) },
                    onClickIcon = { onClickPlaceIcon(place.favoriteId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceListPreview() {
    val samplePlaces = listOf(
        FavoritePlace(
            favoriteId = 1L,
            patientId = 1L,
            placeName = "집",
            placeAlias = "우리집",
            category = "집",
            address = "서울시 강남구 테헤란로 123",
            latitude = 37.50449, longitude = 127.0489,
            isDefault = true, count = 10, createdAt = "2023-10-27T10:00:00Z"
        ),
        FavoritePlace(
            favoriteId = 2L,
            patientId = 1L,
            placeName = "회사",
            placeAlias = null, // 별칭이 없는 경우
            category = "회사",
            address = "서울시 강남구 테헤란로 456",
            latitude = 37.50123, longitude = 127.0395,
            isDefault = false, count = 5, createdAt = "2023-10-27T10:00:00Z"
        )
    )

    PlaceList(
        places = samplePlaces,
        onAddPlaceClick = { },
        onClickPlaceIcon = { },
        onClickPlaceCardWithPatient = { _, _ -> },
        onGoSearchPlaceClick = { }
    )
}
