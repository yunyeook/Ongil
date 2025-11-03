package kr.co.ongil.domain.patient.favorite.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.patient.favorite.dto.request.CreateFavoriteRequest;
import kr.co.ongil.domain.patient.favorite.dto.request.UpdateFavoriteRequest;
import kr.co.ongil.domain.patient.favorite.dto.response.FavoriteListResponse;
import kr.co.ongil.domain.patient.favorite.dto.response.FavoriteResponse;
import kr.co.ongil.domain.patient.favorite.service.FavoriteService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/patients/{patientId}/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorite API", description = "즐겨찾기 관련 API")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "즐겨찾기 등록", description = "환자에게 즐겨찾기 장소를 등록합니다.")
    public ApiResponse<FavoriteResponse> createFavorite(
        @Parameter(description = "환자 ID") @PathVariable Integer patientId,
        @Valid @RequestBody CreateFavoriteRequest request
        // @AuthenticationPrincipal CustomUserDetails userDetails // 실제 인증 사용 시
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        FavoriteResponse response = favoriteService.createFavorite(patientId, request, callerId);
        return ApiResponse.success(ResponseMessage.FAVORITE_CREATED, response);
    }

    @GetMapping
    @Operation(summary = "즐겨찾기 목록 조회", description = "환자의 즐겨찾기 목록을 조회합니다.")
    public ApiResponse<FavoriteListResponse> getFavorites(
        @Parameter(description = "환자 ID") @PathVariable Integer patientId
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        FavoriteListResponse response = favoriteService.getFavorites(patientId, callerId);
        return ApiResponse.success(ResponseMessage.FAVORITE_LIST_FOUND, response);
    }
    @GetMapping("/{favoriteId}")
    @Operation(summary = "즐겨찾기 상세 조회", description = "특정 즐겨찾기의 상세 정보를 조회합니다.")
    public ApiResponse<FavoriteResponse> getFavorite(
        @Parameter(description = "환자 ID") @PathVariable Integer patientId,
        @Parameter(description = "즐겨찾기 ID") @PathVariable Integer favoriteId
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        FavoriteResponse response = favoriteService.getFavorite(patientId, favoriteId, callerId);
        return ApiResponse.success(ResponseMessage.FAVORITE_FOUND, response);
    }

    @PatchMapping("/{favoriteId}")
    @Operation(summary = "즐겨찾기 수정", description = "즐겨찾기 정보를 수정합니다.")
    public ApiResponse<FavoriteResponse> updateFavorite(
        @Parameter(description = "환자 ID") @PathVariable Integer patientId,
        @Parameter(description = "즐겨찾기 ID") @PathVariable Integer favoriteId,
        @RequestBody UpdateFavoriteRequest request
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        FavoriteResponse response = favoriteService.updateFavorite(patientId, favoriteId, request, callerId);
        return ApiResponse.success(ResponseMessage.FAVORITE_UPDATED, response);
    }

    @DeleteMapping("/{favoriteId}")
    @Operation(summary = "즐겨찾기 삭제", description = "즐겨찾기를 삭제합니다.")
    public ApiResponse<String> deleteFavorite(
        @Parameter(description = "환자 ID") @PathVariable Integer patientId,
        @Parameter(description = "즐겨찾기 ID") @PathVariable Integer favoriteId
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        favoriteService.deleteFavorite(patientId, favoriteId, callerId);
        return ApiResponse.success(ResponseMessage.FAVORITE_DELETED,"");

    }

    @PatchMapping("/{favoriteId}/default")
    @Operation(summary = "기본 목적지 설정", description = "해당 즐겨찾기를 기본 목적지로 설정합니다.")
    public ApiResponse<FavoriteResponse> setDefaultFavorite(
        @Parameter(description = "환자 ID") @PathVariable Integer patientId,
        @Parameter(description = "즐겨찾기 ID") @PathVariable Integer favoriteId
    ) {
        Integer callerId = SecurityUtil.getCurrentUserId();
        FavoriteResponse response = favoriteService.setDefaultFavorite(patientId, favoriteId, callerId);
        return ApiResponse.success(ResponseMessage.FAVORITE_DEFAULT_SET, response);
    }
}