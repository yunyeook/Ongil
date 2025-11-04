package kr.co.ongil.presentation.ui.myinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kr.co.ongil.presentation.uistate.CallType
import kr.co.ongil.presentation.uistate.RecentCallUi
import kr.co.ongil.presentation.uistate.RecentCallsEvent
import kr.co.ongil.presentation.viewmodel.RecentCallsViewModel

private val RecentAccent = Color(0xFF8CA898)

/**
 * 최근 통화 목록 화면 (ViewModel 기반)
 */
@Composable
fun RecentCallsScreen(
    modifier: Modifier = Modifier,
    viewModel: RecentCallsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToCallDetail: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    RecentCallsContent(
        searchQuery = uiState.searchQuery,
        calls = uiState.filteredCalls,
        isLoading = uiState.isLoading,
        error = uiState.error,
        onEvent = viewModel::onEvent,
        onNavigateToCallDetail = onNavigateToCallDetail,
        modifier = modifier
    )
}

/**
 * 최근 통화 목록 화면 컨텐츠 (Stateless)
 */
@Composable
private fun RecentCallsContent(
    searchQuery: String,
    calls: List<RecentCallUi>,
    isLoading: Boolean,
    error: String?,
    onEvent: (RecentCallsEvent) -> Unit,
    onNavigateToCallDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
            .imePadding()
    ) {
        // 검색창
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { onEvent(RecentCallsEvent.UpdateSearchQuery(it)) }
            )
        }

        when {
            isLoading -> {
                // 로딩 상태
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = RecentAccent)
                }
            }

            error != null -> {
                // 에러 상태
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                // 통화 목록
                CallList(
                    calls = calls,
                    onEvent = onEvent,
                    onNavigateToCallDetail = onNavigateToCallDetail
                )
            }
        }
    }
}

/**
 * 검색창
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("이름 또는 번호 검색") },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFFE6EBE9),
            unfocusedBorderColor = Color(0xFFF1F3F2),
            cursorColor = RecentAccent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        )
    )
}

/**
 * 통화 목록
 */
@Composable
private fun CallList(
    calls: List<RecentCallUi>,
    onEvent: (RecentCallsEvent) -> Unit,
    onNavigateToCallDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 32.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(calls, key = { it.id }) { call ->
            CallListItem(
                item = call,
                onInfoClick = {
                    onEvent(RecentCallsEvent.OnInfoClick(call))
                    onNavigateToCallDetail(call.id)
                }
            )
        }
    }
}

/**
 * Preview
 */
@Preview(name = "Recent Calls", showBackground = true)
@Composable
private fun PreviewRecentCallsScreen() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        val sampleCalls = listOf(
            RecentCallUi(1, "이지현", CallType.VOIP, "오늘 오후 3:24 · 통화시간 5분 12초"),
            RecentCallUi(2, "박민수", CallType.NORMAL, "오늘 오후 1:02 · 통화시간 1분 03초"),
            RecentCallUi(3, "김수진", CallType.SOS, "오늘 오후 12:10 · 통화시간 20초"),
            RecentCallUi(4, "02-1234-5678", CallType.NORMAL, "어제 오후 9:50 · 통화시간 2분 11초"),
            RecentCallUi(5, "이지현", CallType.VOIP, "어제 오후 8:30 · 통화시간 30초")
        )

        RecentCallsContent(
            searchQuery = "",
            calls = sampleCalls,
            isLoading = false,
            error = null,
            onEvent = {},
            onNavigateToCallDetail = {}
        )
    }
}
