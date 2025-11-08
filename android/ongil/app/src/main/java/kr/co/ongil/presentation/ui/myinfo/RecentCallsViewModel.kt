package kr.co.ongil.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kr.co.ongil.data.mapper.toRecentCallUi
import kr.co.ongil.domain.repository.CallRepository
import kr.co.ongil.presentation.uistate.RecentCallUi
import kr.co.ongil.presentation.uistate.RecentCallsEvent
import kr.co.ongil.presentation.uistate.RecentCallsUiState
import javax.inject.Inject

/**
 * 최근 통화 목록 화면 ViewModel
 */
@HiltViewModel
class RecentCallsViewModel @Inject constructor(
    private val callRepository: CallRepository
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

                // Repository에서 통화 목록 가져오기
                val result = callRepository.getCallLogs()

                result.onSuccess { callLogs ->
                    // TODO: 현재 로그인된 사용자 ID 가져오기 (UserRepository 또는 TokenManager에서)
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
