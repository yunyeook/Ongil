package kr.co.ongil.domain.patient.abnormal.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.global.common.response.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(name = "AbnormalListResponse", description = "이상탐지 목록 응답")
public record AbnormalListResponse(

    @ArraySchema(
        arraySchema = @Schema(description = "이상탐지 목록"),
        schema = @Schema(implementation = AbnormalResponse.class)
    )
    List<AbnormalResponse> abnormals,

    @Schema(description = "페이지 정보", implementation = PageInfo.class)
    PageInfo pageInfo
) {
    public static AbnormalListResponse of( Page<AbnormalResponse> page) {
        return new AbnormalListResponse(
            page.getContent(),
            PageInfo.from(page)
        );
    }
}