package kr.co.ongil.domain.patient.favorite.dto.response;

import kr.co.ongil.domain.patient.favorite.entity.Favorite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class FavoriteResponse {

    private Integer favoriteId;
    private Integer patientId;
    private String placeName;
    private String placeAlias;
    private String category;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer count;
    private Boolean isDefault;
    private Integer displayOrder;
    private LocalDateTime createdAt;

    public static FavoriteResponse from(Favorite favorite) {
        return FavoriteResponse.builder()
            .favoriteId(favorite.getId())
            .patientId(favorite.getPatient().getId())
            .placeName(favorite.getPlaceName())
            .placeAlias(favorite.getPlaceAlias())
            .category(favorite.getCategory())
            .address(favorite.getAddress())
            .latitude(favorite.getLatitude())
            .longitude(favorite.getLongitude())
            .count(favorite.getCount())
            .isDefault(favorite.getIsDefault())
            .displayOrder(favorite.getDisplayOrder())
            .createdAt(favorite.getCreatedAt())
            .build();
    }
}