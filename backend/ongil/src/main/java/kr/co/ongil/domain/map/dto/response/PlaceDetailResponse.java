package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장소 상세 정보")
public record PlaceDetailResponse(

    @Schema(description = "POI ID", example = "7839545")
    String id,

    @Schema(description = "장소명", example = "서울금융복지상담센터 서울시청센터")
    String name,

    @Schema(description = "주소 정보")
    AddressResponse address,

    @Schema(description = "좌표 정보")
    CoordinateResponse coordinate,

    @Schema(description = "카테고리 정보")
    CategoryInfo category,

    @Schema(description = "전화번호", example = "16440120")
    String phoneNumber,

    @Schema(description = "설명", example = "서울시청 내 위치한 금융복지상담센터")
    String description,

    @Schema(description = "우편번호", example = "04520")
    String zipCode,

    @Schema(description = "주차 가능 여부")
    Boolean parking,

    @Schema(description = "영업 정보")
    BusinessInfo businessInfo
) {


    public static PlaceDetailResponse of(
        String id,
        String name,
        AddressResponse address,
        CoordinateResponse coordinate,
        CategoryInfo category,
        String phoneNumber,
        String description,
        String zipCode,
        Boolean parking,
        BusinessInfo businessInfo
    ) {
        return new PlaceDetailResponse(
            id, name, address, coordinate, category,
            phoneNumber, description, zipCode, parking, businessInfo
        );
    }
}