package kr.co.ongil.domain.model

/**
 * 장소 검색 결과 도메인 모델
 */
data class SearchPlace(
    val name: String,           // 장소명
    val address: String,        // 주소
    val latitude: Double,       // 위도
    val longitude: Double,      // 경도
    val distance: Int? = null,  // 거리 (미터)
    val category: String? = null // 카테고리
)