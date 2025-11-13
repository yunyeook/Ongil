package kr.co.ongil.domain.patient.favorite.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CreateFavoriteRequest", description = "즐겨찾기 장소 등록")
public record CreateFavoriteRequest (

    @NotBlank(message = "장소명은 필수입니다.")
    @Schema(description = "실제 장소명", example = "경희도봉산한의원")
    String placeName,

    @Schema(description = "장소 별칭", example = "자주가는한의원")
    String placeAlias,

    @NotBlank(message = "카테고리는 필수입니다.")
    @Schema(description = "장소 카테고리", example = "집")
    String category,

    @NotBlank(message = "주소는 필수입니다.")
    @Schema(description = "주소", example = "서울특별시 도봉구 도봉로 689")
    String address,

    @NotNull(message = "위도는 필수입니다.")
    @Schema(description = "위도", example = "37.6521")
    Double latitude,

    @NotNull(message = "경도는 필수입니다.")
    @Schema(description = "경도", example = "127.0342")
    Double longitude,

    @Schema(description = "기본목적지 여부")
    Boolean isDefault
){

    public String getPlaceAlias() {
        return placeAlias != null ? placeAlias : placeName;
    }

    public Boolean getIsDefault() {
        return isDefault != null ? isDefault : false;
    }
}