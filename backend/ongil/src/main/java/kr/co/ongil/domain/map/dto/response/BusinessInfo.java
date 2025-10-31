package kr.co.ongil.domain.map.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "영업 정보")
public record BusinessInfo(
    @Schema(description = "영업시간", example = "월~금 09:00~18:00")
    String businessHours,

    @Schema(description = "휴무일", example = "토, 일요일")
    String closedDays,

    @Schema(description = "24시간 영업 여부")
    Boolean is24Hours,

    @Schema(description = "연중무휴 여부")
    Boolean isYearRound
) {
    /**
     * additionalInfo 파싱해서 BusinessInfo 생성
     */
    public static BusinessInfo fromAdditionalInfo(String additionalInfo, String twFlag, String yaFlag) {
        String businessHours = null;
        String closedDays = null;

        if (additionalInfo != null && !additionalInfo.isEmpty()) {
            // [영업시간] 파싱
            if (additionalInfo.contains("[영업시간]")) {
                int start = additionalInfo.indexOf("[영업시간]") + 6;
                int end = additionalInfo.indexOf(";[", start);
                if (end == -1) {
                    end = additionalInfo.length();
                }
                String hoursSection = additionalInfo.substring(start, end);
                businessHours = hoursSection.replace(";", "\n").trim();
            }

            // [휴무] 파싱
            if (additionalInfo.contains("[휴무]")) {
                int start = additionalInfo.indexOf("[휴무]") + 4;
                int end = additionalInfo.indexOf(";", start);
                if (end == -1) {
                    end = additionalInfo.length();
                }
                closedDays = additionalInfo.substring(start, end).trim();
            }
        }

        Boolean is24Hours = twFlag != null && twFlag.equals("1");
        Boolean isYearRound = yaFlag != null && yaFlag.equals("1");

        return new BusinessInfo(businessHours, closedDays, is24Hours, isYearRound);
    }
}