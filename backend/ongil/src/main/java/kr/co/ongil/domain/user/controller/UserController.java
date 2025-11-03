package kr.co.ongil.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.user.dto.request.UpdateUserRequest;
import kr.co.ongil.domain.user.dto.response.UpdateUserResponse;
import kr.co.ongil.domain.user.dto.response.UserResponse;
import kr.co.ongil.domain.user.service.UserService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.security.userdetails.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User API", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {

        UserResponse userResponse = userService.getMe(userDetails.getUserId());
        return ApiResponse.success(ResponseMessage.USER_FOUND, userResponse);
    }

    @PatchMapping("/me")
    @Operation(summary = "내 정보 수정", description = "현재 로그인한 사용자의 정보를 수정합니다.")
    public ApiResponse<UpdateUserResponse> updateMe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute UpdateUserRequest request) {

        UpdateUserResponse updateUserResponse = userService.updateMe(userDetails.getUserId(), request);
        return ApiResponse.success(ResponseMessage.USER_UPDATED, updateUserResponse);
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원 탈퇴", description = "본인 계정을 삭제합니다. (소프트 삭제)")
    public ApiResponse<String> deleteMe(@AuthenticationPrincipal CustomUserDetails userDetails) {

        userService.deleteMe(userDetails.getUserId());
        return ApiResponse.success(ResponseMessage.USER_DELETE_SUCCESS);
    }
}
