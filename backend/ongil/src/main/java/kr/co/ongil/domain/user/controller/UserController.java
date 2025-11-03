package kr.co.ongil.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.ongil.domain.user.service.UserService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User API", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "본인 계정을 삭제합니다. (소프트 삭제)")
    public ApiResponse<String> deleteMe(@AuthenticationPrincipal Integer userId) {

        userService.deleteMe(userId);
        return ApiResponse.success(ResponseMessage.USER_DELETE_SUCCESS);
    }
}
