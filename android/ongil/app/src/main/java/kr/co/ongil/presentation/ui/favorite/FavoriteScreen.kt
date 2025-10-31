package kr.co.ongil.presentation.ui.favorite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FavoriteScreen(
    onNavigateToPlaceDetail: (Long) -> Unit,
    viewModel: FavoriteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9FAFB) // 전체 배경 (연한 회색 톤)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // 1. 상단 헤더 영역 (프로필, 알림)
            TopHeaderBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            )

            // 2. 타이틀 / 설명 영역
            FavoriteTitleSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 탭 바 (환자 목록 / 장소 목록)
            FavoriteTabBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = { tab ->
                    viewModel.onEvent(FavoriteUiEvent.OnTabSelected(tab))
                },
                modifier = Modifier
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. 탭 컨텐츠
            when (uiState.selectedTab) {
                FavoriteTab.PATIENTS -> {
                    PatientList(
                        patients = uiState.patients,
                        onCallClick = { id ->
                            viewModel.onEvent(FavoriteUiEvent.OnCallClick(id))
                        },
                        onAddPatientClick = {
                            viewModel.onEvent(FavoriteUiEvent.OnAddPatientClick)
                        }
                    )
                }

                FavoriteTab.PLACES -> {
                    PlaceList(
                        places = uiState.places,
                        onAddPlaceClick = {
                            viewModel.onEvent(FavoriteUiEvent.OnAddPlaceClick)
                        },
                        onClickPlaceCard = { placeId ->
                            onNavigateToPlaceDetail(placeId)
                        },
                        onClickPlaceIcon = { placeId ->
                            // TODO: 지도 화면으로 이동 or 지도 열기 처리 예정
                        }
                    )
                }
            }

            // 5. 하단 네비게이션 바 영역
            // 이건 presentation/ui/common 하단탭 컴포넌트 만들어서 붙일 예정
        }
    }
}



// 헤더 일단 임시로 만들어뒀음
@Composable
private fun TopHeaderBar(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 왼쪽: 사용자 프로필 영역 (지금은 텍스트로 대체)
        Text(
            text = "온길",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.weight(1f))

        // 오른쪽: 알림 아이콘
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "알림",
            tint = Color(0xFF4B5563),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * 타이틀 / 보조설명
 * "사용자님의 즐겨찾기" 같은 부분입니다.
 * 실제 문구/스타일은 시안에 맞춰 조정 가능
 */
@Composable
private fun FavoriteTitleSection(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "사용자님의 즐겨찾기",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "자주 연락하는 보호 대상자와 관리 장소를 한 곳에서 확인하세요.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF6B7280),
            lineHeight = 20.sp
        )
    }
}

/**
 * 장소 탭이 아직 준비 안 된 상태라서 임시로 화면 비지 않게 넣는 플레이스홀더
 * 추후 PlaceListSection으로 교체
 */
@Composable
private fun PlaceholderPlacesSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "즐겨찾는 장소",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF4B5563)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "등록된 장소가 없습니다.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF9CA3AF),
            lineHeight = 20.sp
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun FavoriteScreenPreviewPatients() {

    val dummyPatients = listOf(
        PatientItem(
            id = 1L,
            displayName = "김철수 (남, 75세)",
            phoneNumber = "010-1234-5678"
        ),
        PatientItem(
            id = 2L,
            displayName = "이영희 (여, 68세)",
            phoneNumber = "010-2345-6789"
        )
    )

    Surface(color = Color(0xFFF9FAFB)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopHeaderBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            )

            FavoriteTitleSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            FavoriteTabBar(
                selectedTab = FavoriteTab.PATIENTS,
                onTabSelected = {},
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            PatientList(
                patients = dummyPatients,
                onCallClick = {},
                onAddPatientClick = {}
            )
        }
    }
}