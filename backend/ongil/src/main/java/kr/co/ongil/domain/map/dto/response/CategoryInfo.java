package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

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