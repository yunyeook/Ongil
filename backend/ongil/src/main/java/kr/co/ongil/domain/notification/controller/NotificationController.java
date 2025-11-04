package kr.co.ongil.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import kr.co.ongil.domain.notification.dto.request.NotificationRequest;
import kr.co.ongil.domain.notification.dto.response.NotificationListResponse;
import kr.co.ongil.domain.notification.dto.response.NotificationReadResponse;
import kr.co.ongil.domain.notification.dto.response.NotificationResponse;
import kr.co.ongil.domain.notification.service.NotificationService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification API", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "로그인한 회원의 알림 목록을 조회합니다.")
    public ApiResponse<NotificationListResponse> getNotifications(
        // @AuthenticationPrincipal CustomUserDetails userDetails,
        @Parameter(description = "조회할 페이지 번호 (1부터 시작)") @RequestParam(defaultValue = "1") int page,
        @Parameter(description = "페이지당 알림 개수") @RequestParam(defaultValue = "10") int size,
        @Parameter(description = "읽음 여부 필터") @RequestParam(required = false) Boolean read
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        NotificationListResponse response = notificationService.getNotifications(userId, page, size, read);
        return ApiResponse.success(ResponseMessage.NOTIFICATION_LIST_FOUND, response);
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    public ApiResponse<NotificationReadResponse> markAsRead(
        @Parameter(description = "알림 ID") @PathVariable Integer notificationId
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        NotificationReadResponse response = notificationService.markAsRead(userId, notificationId);
        return ApiResponse.success(ResponseMessage.NOTIFICATION_READ, response);
    }

    @PatchMapping("/read")
    @Operation(summary = "전체 알림 읽음 처리", description = "전체 알림을 읽음 처리합니다.")
    public ApiResponse<String> markAsReadAll(
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        notificationService.markAsReadAll(userId);
        return ApiResponse.success(ResponseMessage.NOTIFICATION_READ_ALL);
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    public ApiResponse<Map<String,Integer>> deleteNotification(
        @Parameter(description = "알림 ID") @PathVariable Integer notificationId
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        Map<String,Integer>deletedNotification= notificationService.deleteNotification(userId, notificationId);
        return ApiResponse.success(ResponseMessage.NOTIFICATION_DELETED, deletedNotification);
    }

    @DeleteMapping
    @Operation(summary = "전체 알림 삭제", description = "전체 알림을 삭제합니다.")
    public ApiResponse<Map<String,Integer>>  deleteAllNotifications(
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        Map<String,Integer>deletedNotifications=notificationService.deleteAllNotifications(userId);
        return ApiResponse.success(ResponseMessage.NOTIFICATION_DELETED_ALL,deletedNotifications);
    }

    @PostMapping
    @Operation(summary = "알림 생성", description = "알림을 생성합니다.")
    public ApiResponse<List<NotificationResponse>> createNotification(@RequestBody NotificationRequest request) {
        List<NotificationResponse> response = notificationService.createNotifications(request);
        return ApiResponse.success(ResponseMessage.NOTIFICATION_CREATED, response);
    }
}