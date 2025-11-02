package kr.co.ongil.domain.map.dto.tmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmapPoiDetailResponse(
    PoiDetailInfo poiDetailInfo
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PoiDetailInfo(

        String id,

        String name,

        String tel,

        String frontLat,

        String frontLon,

        String lat,

        String lon,

        String lcdName,

        String mcdName,

        String scdName,

        String dcdName,

        String firstNo,

        String secondNo,

        String bldAddr,

        String roadName,

        String bldNo1,

        String bldNo2,

        String bizCatName,

        String desc,

        String zipCode,

        String parkFlag,

        String twFlag,  // 24시간 영업 여부

        String yaFlag,  // 연중무휴 여부

        String additionalInfo  // 영업시간/휴무 정보
    ) {
    }
}