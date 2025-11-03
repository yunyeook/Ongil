package kr.co.ongil.domain.patient.favorite.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class FavoriteListResponse {

    private Integer totalCount;
    private List<FavoriteResponse> favorites;

    public static FavoriteListResponse of(List<FavoriteResponse> favorites) {
        return FavoriteListResponse.builder()
            .totalCount(favorites.size())
            .favorites(favorites)
            .build();
    }
}