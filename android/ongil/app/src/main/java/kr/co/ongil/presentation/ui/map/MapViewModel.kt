package kr.co.ongil.presentation.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kr.co.ongil.domain.model.SearchPlace
import kr.co.ongil.domain.repository.MapRepository
import kr.co.ongil.domain.usecase.map.SearchPlaceUseCase
import kr.co.ongil.common.location.LocationStreamBus
import javax.inject.Inject

/**
 * 지도 화면 ViewModel
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    val locationBus: LocationStreamBus,
    private val searchPlaceUseCase: SearchPlaceUseCase,
    private val mapRepository: MapRepository
) : ViewModel() {

    // 검색어
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // 검색 결과 (실시간 TMap 검색)
    private val _searchResults = MutableStateFlow<List<SearchPlace>>(emptyList())
    val searchResults: StateFlow<List<SearchPlace>> = _searchResults.asStateFlow()

    // 최종 검색 결과 (백엔드 API 검색)
    private val _finalSearchResults = MutableStateFlow<List<SearchPlace>?>(null)
    val finalSearchResults: StateFlow<List<SearchPlace>?> = _finalSearchResults.asStateFlow()

    // 로딩 상태
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    init {
        // 검색어 변경 시 debounce 적용하여 자동 검색
        @OptIn(FlowPreview::class)
        viewModelScope.launch {
            _searchQuery
                .debounce(500) // 500ms 대기
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isNotBlank()) {
                        searchPlaces(query)
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }
    }

    /**
     * 검색어 업데이트
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * 장소 검색 (실시간 TMap 검색)
     */
    private suspend fun searchPlaces(query: String) {
        _isSearching.value = true

        val location = locationBus.lastValue
        Log.d("MapViewModel", "장소 검색: $query (lat: ${location?.latitude}, lng: ${location?.longitude})")

        searchPlaceUseCase(
            query = query,
            latitude = location?.latitude,
            longitude = location?.longitude
        )
            .onSuccess { places ->
                _searchResults.value = places
                Log.d("MapViewModel", "검색 성공: ${places.size}개")
            }
            .onFailure { e ->
                Log.e("MapViewModel", "검색 실패: ${e.message}", e)
                _searchResults.value = emptyList()
            }

        _isSearching.value = false
    }

    /**
     * 검색 결과 초기화
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }

    /**
     * 최종 검색 (백엔드 API 호출)
     * 검색 버튼을 눌렀을 때 호출
     */
    fun onFinalSearch(radius: Int? = 3000) {
        Log.d("MapViewModel", "onFinalSearch 호출됨")
        viewModelScope.launch {
            val query = _searchQuery.value
            Log.d("MapViewModel", "검색어: $query")
            if (query.isBlank()) {
                Log.d("MapViewModel", "검색어가 비어있음")
                return@launch
            }

            val location = locationBus.lastValue
            val latitude = location?.latitude
            val longitude = location?.longitude

            _isSearching.value = true
            Log.d("MapViewModel", "최종 검색 시작: $query (lat: $latitude, lng: $longitude)")

            mapRepository.searchPlaces(
                query = query,
                latitude = latitude,
                longitude = longitude,
                radius = radius
            )
                .onSuccess { places ->
                    _finalSearchResults.value = places
                    Log.d("MapViewModel", "최종 검색 성공: ${places.size}개")
                }
                .onFailure { e ->
                    Log.e("MapViewModel", "최종 검색 실패: ${e.message}", e)
                    _finalSearchResults.value = emptyList()
                }

            _isSearching.value = false
        }
    }

    /**
     * 최종 검색 결과 모달 닫기
     */
    fun closeFinalSearchResults() {
        _finalSearchResults.value = null
    }
}