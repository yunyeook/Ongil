package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.data.model.call.toRecentCallUi
import kr.co.ongil.domain.repository.CallRepository
import kr.co.ongil.presentation.uistate.CallType
import kr.co.ongil.presentation.uistate.RecentCallUi
import kr.co.ongil.presentation.uistate.RecentCallsEvent
import kr.co.ongil.presentation.uistate.RecentCallsUiState

/**
 * 최근 통화 목록 화면 ViewModel
 */
class RecentCallsViewModel(
    private val callRepository: CallRepository? = null
    // TODO: DI(Hilt/Koin)로 주입하도록 변경
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecentCallsUiState())
    val uiState: StateFlow<RecentCallsUiState> = _uiState.asStateFlow()

    init {
        loadCalls()
    }

    /**
     * 이벤트 처리
     */
    fun onEvent(event: RecentCallsEvent) {
        when (event) {
            is RecentCallsEvent.UpdateSearchQuery -> {
                updateSearchQuery(event.query)
            }
            is RecentCallsEvent.OnInfoClick -> {
                handleInfoClick(event.call)
            }
            is RecentCallsEvent.LoadCalls -> {
                loadCalls()
            }
        }
    }

    /**
     * 검색어 업데이트 및 필터링
     */
    private fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filtered = if (query.isBlank()) {
                currentState.calls
            } else {
                currentState.calls.filter {
                    it.nameOrNumber.contains(query, ignoreCase = true)
                }
            }
            currentState.copy(
                searchQuery = query,
                filteredCalls = filtered
            )
        }
    }

    /**
     * 통화 목록 로드
     */
    private fun loadCalls() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                if (callRepository == null) {
                    // Repository가 없으면 Mock 데이터 사용
                    val mockCalls = listOf(
                        RecentCallUi(1, "이지현", CallType.VOIP, "오늘 오후 3:24 · 통화시간 5분 12초"),
                        RecentCallUi(2, "박민수", CallType.NORMAL, "오늘 오후 1:02 · 통화시간 1분 03초"),
                        RecentCallUi(3, "김수진", CallType.SOS, "오늘 오후 12:10 · 통화시간 20초"),
                        RecentCallUi(4, "02-1234-5678", CallType.NORMAL, "어제 오후 9:50 · 통화시간 2분 11초"),
                        RecentCallUi(5, "이지현", CallType.VOIP, "어제 오후 8:30 · 통화시간 30초")
                    )

                    _uiState.update {
                        it.copy(
                            calls = mockCalls,
                            filteredCalls = mockCalls,
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // Repository에서 통화 목록 가져오기
                val result = callRepository.getCallLogs()

                result.onSuccess { callLogs ->
                    // TODO: 현재 로그인된 사용자 ID 가져오기
                    val currentUserId = 1L

                    // CallLogDto → RecentCallUi 변환
                    val calls = callLogs.map { it.toRecentCallUi(currentUserId) }

                    _uiState.update {
                        it.copy(
                            calls = calls,
                            filteredCalls = calls,
                            isLoading = false
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "통화 목록을 불러오는데 실패했습니다."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "통화 목록을 불러오는데 실패했습니다."
                    )
                }
            }
        }
    }

    /**
     * 상세 정보 버튼 클릭 처리
     */
    private fun handleInfoClick(call: RecentCallUi) {
        // TODO: 상세 화면으로 이동
        // 예: navigationManager.navigateToCallDetail(call.id)
        println("상세 정보: ${call.nameOrNumber}")
    }
}
