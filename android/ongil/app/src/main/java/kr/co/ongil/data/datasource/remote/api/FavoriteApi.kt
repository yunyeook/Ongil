package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.favorite.PlaceDetailDto
import kr.co.ongil.data.model.favorite.FavoritePlacesDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import kr.co.ongil.data.model.favorite.PlaceDetailUpdateDto

interface FavoriteApi {
    @GET("/api/v1/patients/{patientId}/favorites")
    suspend fun getFavoritePlaces(
        @Path("patientId") patientId: Long
    ): Response<FavoritePlacesDto>

    @GET("/api/v1/patients/{patientId}/favorites/{favoriteId}")
    suspend fun getPlaceDetail(
        @Path("patientId") patientId: Long,
        @Path("favoriteId") favoriteId: Long
    ): Response<PlaceDetailDto>

    @PATCH("/api/v1/patients/{patientId}/favorites/{favoriteId}")
    suspend fun updateFavoritePlace(
        @Path("patientId") patientId: Long,
        @Path("favoriteId") favoriteId: Long,
        @Body body: PlaceDetailUpdateDto
    ): Response<PlaceDetailDto>

    @DELETE("/api/v1/patients/{patientId}/favorites/{favoriteId}")
    suspend fun deleteFavoritePlace(
        @Path("patientId") patientId: Long,
        @Path("favoriteId") favoriteId: Long
    ): Response<Unit>
}