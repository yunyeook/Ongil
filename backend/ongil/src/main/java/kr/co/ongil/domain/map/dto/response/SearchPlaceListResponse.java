package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.global.common.response.PageInfo;
import java.util.List;

@Schema(description = "장소 검색 결과 목록")
public record SearchPlaceListResponse(

    @Schema(description = "페이지네이션 정보")
    PageInfo pageInfo,

    @Schema(description = "장소 목록")
    List<SearchPlaceResponse> places
) {

    public static SearchPlaceListResponse of(Integer totalCount, Integer currentPage,
        Integer pageSize, List<SearchPlaceResponse> places) {
        PageInfo pageInfo = PageInfo.of(totalCount, currentPage, pageSize);
        return new SearchPlaceListResponse(pageInfo, places);
    }
}