package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주소 정보 응답")
public record AddressResponse(

    @Schema(description = "도로명 주소", example = "서울 중구 세종대로 110")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울 중구 태평로1가 31")
    String jibunAddress
) {

    public static AddressResponse from(String roadNameAddress, String lotNumberAddress) {
        return new AddressResponse(roadNameAddress, lotNumberAddress);
    }
}