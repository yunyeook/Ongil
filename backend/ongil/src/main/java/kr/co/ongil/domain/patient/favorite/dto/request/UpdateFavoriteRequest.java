package kr.co.ongil.domain.patient.favorite.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateFavoriteRequest", description = "즐겨찾기 장소 수정")

public record UpdateFavoriteRequest (

    @Schema(description = "실제 장소명", example = "경희도봉산한의원")
    String placeName,

    @Schema(description = "장소 별칭", example = "자주가는한의원")
    String placeAlias,

    @Schema(description = "장소 카테고리", example = "집")
    String category,

    @Schema(description = "주소", example = "서울특별시 도봉구 도봉로 689")
    String address,

    @Schema(description = "위도", example = "37.6521")
    Double latitude,

    @Schema(description = "경도", example = "127.0342")
    Double longitude,

    @Schema(description = "기본목적지 여부")
    Boolean isDefault
){

    public boolean hasAnyUpdate() {
        return placeName != null || placeAlias != null || category != null
            || address != null || latitude != null || longitude != null ||isDefault!=null;
    }
}