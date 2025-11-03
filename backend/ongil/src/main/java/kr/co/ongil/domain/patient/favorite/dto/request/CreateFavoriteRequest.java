package kr.co.ongil.domain.patient.favorite.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFavoriteRequest {

    @NotBlank(message = "장소명은 필수입니다.")
    private String placeName;

    private String placeAlias;

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;

    @NotNull(message = "위도는 필수입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수입니다.")
    private Double longitude;

    private Boolean isDefault;

    public String getPlaceAlias() {
        return placeAlias != null ? placeAlias : placeName;
    }

    public Boolean getIsDefault() {
        return isDefault != null ? isDefault : false;
    }
}