package kr.co.ongil.data.repository

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.ongil.data.datasource.remote.api.MapApi
import kr.co.ongil.data.mapper.toDomain
import kr.co.ongil.domain.model.SearchPlace
import kr.co.ongil.domain.repository.MapRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapRepositoryImpl @Inject constructor(
    private val mapApi: MapApi
) : MapRepository {

    override suspend fun searchPlaces(
        query: String,
        latitude: Double?,
        longitude: Double?,
        radius: Int?,
        page: Int,
        size: Int
    ): Result<List<SearchPlace>> = withContext(Dispatchers.IO) {
        try {
            if (query.isBlank()) {
                return@withContext Result.success(emptyList())
            }

            Log.d("MapRepository", "장소 검색: $query")

            val response = mapApi.searchPlaces(
                keyword = query,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                page = page,
                size = size
            )

            val places = response.data.places.map { it.toDomain() }
            Log.d("MapRepository", "검색 성공: ${places.size}개 결과")

            Result.success(places)
        } catch (e: Exception) {
            Log.e("MapRepository", "검색 오류: ${e.message}", e)
            Result.failure(e)
        }
    }
}
