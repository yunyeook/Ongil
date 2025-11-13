package kr.co.ongil.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.ongil.global.common.response.PageInfo;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(name = "NotificationListResponse", description = "알림 목록 응답")
public record NotificationListResponse(
    @ArraySchema(
        arraySchema = @Schema(description = "알림 목록"),
        schema = @Schema(implementation = NotificationResponse.class)
    )
    List<NotificationResponse> notifications,

    @Schema(description = "페이지 정보", implementation = PageInfo.class)
    PageInfo pageInfo
) {
    public static NotificationListResponse of(Page<NotificationResponse> page) {
        return new NotificationListResponse(
            page.getContent(),
            PageInfo.from(page)
        );
    }
}
