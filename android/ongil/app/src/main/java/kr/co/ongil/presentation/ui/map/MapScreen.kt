package kr.co.ongil.presentation.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kr.co.ongil.presentation.ui.common.map.CircleFloatingButton
import kr.co.ongil.presentation.ui.common.map.SearchBar
import kr.co.ongil.presentation.ui.common.map.SearchListItem

/**
 * 지도 화면
 * - TMap 표시
 * - 장소 검색 (실시간)
 * - 도움요청 토글 버튼 (플로팅 버튼)
 */
@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    viewModel: MapViewModel = hiltViewModel()
) {
    // ViewModel 상태
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    // 도움요청 토글 상태
    var isSosEnabled by remember { mutableStateOf(false) }

    Box(modifier) {
        // TMap 표시
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = 0.dp
                )
        ) {
            TMapComposable(modifier)
        }

        // 검색바 + 검색 결과 (화면 상단)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // 검색 입력 필드
            SearchBar(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) }
            )

            // 검색 결과 리스트
            if (searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .background(Color.White, androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                ) {
                    items(searchResults) { place ->
                        SearchListItem(
                            placeName = place.name,
                            address = place.address,
                            etaText = "", // TODO: 거리 계산
                            onClick = {
                                // TODO: 지도에 마커 표시 및 이동
                                viewModel.clearSearch()
                            }
                        )
                    }
                }
            }
        }

        // 플로팅 버튼들 (화면 오른쪽 하단)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            // 도움요청 토글 버튼
            CircleFloatingButton(
                icon = Icons.Default.Warning,
                isToggled = isSosEnabled,
                onClick = {
                    isSosEnabled = !isSosEnabled
                    // TODO: 도움요청 토글 상태 변경 시 로직
                    if (isSosEnabled) {
                        // 도움요청 활성화
                    } else {
                        // 도움요청 비활성화
                    }
                }
            )

            // TODO: 다른 플로팅 버튼 추가 (예: 길찾기, 현재 위치, 설정 등)
            // CircleFloatingButton(
            //     icon = Icons.Default.Navigation,
            //     onClick = { /* 길찾기 */ }
            // )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MapScreen(paddingValues = PaddingValues())
}
