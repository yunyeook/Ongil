package kr.co.ongil.domain.usecase.map

import kr.co.ongil.domain.model.SearchPlace
import kr.co.ongil.domain.repository.TMapRepository
import javax.inject.Inject

/**
 * 장소 검색 UseCase
 */
class SearchPlaceUseCase @Inject constructor(
    private val tMapRepository: TMapRepository
) {
    suspend operator fun invoke(
        query: String,
        latitude: Double? = null,
        longitude: Double? = null
    ): Result<List<SearchPlace>> {
        return tMapRepository.searchPlaces(query, latitude, longitude)
    }
}
