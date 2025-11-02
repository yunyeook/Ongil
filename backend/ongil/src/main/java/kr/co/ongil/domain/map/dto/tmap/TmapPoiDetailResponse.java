package kr.co.ongil.domain.map.dto.tmap;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TmapPoiDetailResponse(
    @JsonProperty("poiDetailInfo")
    PoiDetailInfo poiDetailInfo
) {
    public record PoiDetailInfo(

        @JsonProperty("id")
        String id,

        @JsonProperty("name")
        String name,

        @JsonProperty("tel")
        String tel,

        @JsonProperty("frontLat")
        String frontLat,

        @JsonProperty("frontLon")
        String frontLon,

        @JsonProperty("lat")
        String lat,

        @JsonProperty("lon")
        String lon,

        @JsonProperty("lcdName")
        String lcdName,

        @JsonProperty("mcdName")
        String mcdName,

        @JsonProperty("scdName")
        String scdName,

        @JsonProperty("dcdName")
        String dcdName,

        @JsonProperty("firstNo")
        String firstNo,

        @JsonProperty("secondNo")
        String secondNo,

        @JsonProperty("bldAddr")
        String bldAddr,

        @JsonProperty("roadName")
        String roadName,

        @JsonProperty("bldNo1")
        String bldNo1,

        @JsonProperty("bldNo2")
        String bldNo2,

        @JsonProperty("bizCatName")
        String bizCatName,

        @JsonProperty("desc")
        String desc,

        @JsonProperty("zipCode")
        String zipCode,

        @JsonProperty("parkFlag")
        String parkFlag,

        @JsonProperty("twFlag")
        String twFlag,  // 24시간 영업 여부

        @JsonProperty("yaFlag")
        String yaFlag,  // 연중무휴 여부

        @JsonProperty("additionalInfo")
        String additionalInfo  // 영업시간/휴무 정보
    ) {
    }
}