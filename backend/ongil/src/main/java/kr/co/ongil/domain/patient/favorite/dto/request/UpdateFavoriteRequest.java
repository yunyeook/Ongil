package kr.co.ongil.domain.patient.favorite.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFavoriteRequest {

    private String placeName;
    private String placeAlias;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;

    public boolean hasAnyUpdate() {
        return placeName != null || placeAlias != null || category != null
            || address != null || latitude != null || longitude != null;
    }
}