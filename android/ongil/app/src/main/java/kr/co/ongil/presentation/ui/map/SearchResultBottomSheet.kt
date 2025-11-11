package kr.co.ongil.presentation.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.ongil.domain.model.SearchPlace
import kr.co.ongil.presentation.ui.common.map.SearchListItem

/**
 * 검색 결과 BottomSheet
 * 백엔드 API 검색 결과를 모달로 표시
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultBottomSheet(
    searchResults: List<SearchPlace>,
    onDismiss: () -> Unit,
    onPlaceClick: (SearchPlace) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(horizontal = 16.dp)
        ) {
            // 헤더
            Text(
                text = "검색 결과",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF101828),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 결과 개수
            Text(
                text = "${searchResults.size}개의 장소",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Divider(
                color = Color(0xFFE5E7EB),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 검색 결과 리스트
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "검색 결과가 없습니다",
                        fontSize = 16.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(searchResults) { place ->
                        SearchListItem(
                            placeName = place.name,
                            address = place.address,
                            etaText = place.distance?.let { "${it}m" } ?: "",
                            onClick = {
                                onPlaceClick(place)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
