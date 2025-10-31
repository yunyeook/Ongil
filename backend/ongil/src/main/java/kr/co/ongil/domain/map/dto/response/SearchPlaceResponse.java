package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장소 정보")
public record SearchPlaceResponse(

    @Schema(description = "장소명", example = "온길약국")
    String name,

    @Schema(description = "주소", example = "서울 강남구 역삼동 123")
    String address,

    @Schema(description = "위도", example = "37.5665")
    Double latitude,

    @Schema(description = "경도", example = "126.9780")
    Double longitude,

    @Schema(description = "거리(미터)", example = "120")
    Integer distance,

    @Schema(description = "카테고리")
    CategoryInfo category,

    @Schema(description = "전화번호", example = "02-1234-5678")
    String phoneNumber
) {

    @Schema(description = "카테고리 정보")
    public record CategoryInfo(
        @Schema(description = "대분류", example = "생활편의")
        String upperCategory,

        @Schema(description = "중분류", example = "음식점")
        String middleCategory,

        @Schema(description = "소분류", example = "한식")
        String lowerCategory
    ) {
        public static CategoryInfo of(String upper, String middle, String lower) {
            return new CategoryInfo(upper, middle, lower);
        }
    }

    public static SearchPlaceResponse of(String name, String address, Double latitude, Double longitude,
        Integer distance, CategoryInfo category, String phoneNumber) {
        return new SearchPlaceResponse(name, address, latitude, longitude, distance, category, phoneNumber);
    }
}