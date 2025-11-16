package kr.co.ongil.data.datasource.remote.api

import kr.co.ongil.data.model.favorite.PlaceDetailDto
import kr.co.ongil.data.model.favorite.FavoritePlacesDto
import kr.co.ongil.data.model.favorite.RelationshipsResponseDto
import kr.co.ongil.data.model.favorite.RelationshipDetailDto
import kr.co.ongil.data.model.favorite.AddFavoriteRequestDto
import kr.co.ongil.data.model.favorite.DeleteFavoriteResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.DELETE
import kr.co.ongil.data.model.favorite.PlaceDetailUpdateDto

interface FavoriteApi {
    // 장소 관련 API
    @GET("/api/v1/patients/{patientId}/favorites")
    suspend fun getFavoritePlaces(
        @Path("patientId") patientId: Long
    ): Response<FavoritePlacesDto>

    @POST("/api/v1/patients/{patientId}/favorites")
    suspend fun addFavoritePlace(
        @Path("patientId") patientId: Long,
        @Body body: AddFavoriteRequestDto
    ): Response<PlaceDetailDto>

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
    ): Response<DeleteFavoriteResponseDto>

    // 사용자(환자/보호자) 관계 API
    @GET("/api/v1/relationships/me")
    suspend fun getMyRelationships(): Response<RelationshipsResponseDto>

    @GET("/api/v1/relationships/{relationshipId}")
    suspend fun getRelationshipDetail(
        @Path("relationshipId") relationshipId: Long
    ): Response<RelationshipDetailDto>
}