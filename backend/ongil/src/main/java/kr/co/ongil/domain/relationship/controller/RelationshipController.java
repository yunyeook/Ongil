package kr.co.ongil.domain.relationship.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.ongil.domain.relationship.dto.request.CreateRelationshipRequest;
import kr.co.ongil.domain.relationship.dto.request.UpdateRelationshipRequest;
import kr.co.ongil.domain.relationship.dto.response.RelationshipResponse;
import kr.co.ongil.domain.relationship.service.RelationshipService;
import kr.co.ongil.global.common.response.ApiResponse;
import kr.co.ongil.global.common.response.ResponseMessage;
import kr.co.ongil.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/relationships")
@RequiredArgsConstructor
@Tag(name = "Relationship API", description = "보호자-환자 관계 관리 API")
public class RelationshipController {

    private final RelationshipService relationshipService;

    /**
     * 관계 등록 (verificationToken 사용)
     */
    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "관계 등록", description = "전화번호 인증 후 받은 토큰으로 보호자-환자 관계를 등록합니다.")
    public ApiResponse<RelationshipResponse> createRelationship(
            @Valid @RequestBody CreateRelationshipRequest request
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        RelationshipResponse response = relationshipService.createRelationship(userId, request);
        return ApiResponse.success(ResponseMessage.RELATIONSHIP_CREATED, response);
    }

    /**
     * 내 관계 목록 조회
     */
    @GetMapping("/me")
    @Operation(summary = "관계 목록 조회", description = "내가 등록한 보호자/환자 관계 목록을 조회합니다.")
    public ApiResponse<List<RelationshipResponse>> getMyRelationships() {
        Integer userId = SecurityUtil.getCurrentUserId();
        List<RelationshipResponse> response = relationshipService.getMyRelationships(userId);
        return ApiResponse.success(ResponseMessage.RELATIONSHIP_LIST_FOUND, response);
    }

    /**
     * 관계 상세 조회
     */
    @GetMapping("/{relationshipId}")
    @Operation(summary = "관계 상세 조회", description = "특정 관계의 상세 정보를 조회합니다.")
    public ApiResponse<RelationshipResponse> getRelationshipDetail(
            @Parameter(description = "관계 ID", example = "1", required = true)
            @PathVariable Integer relationshipId
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        RelationshipResponse response = relationshipService.getRelationshipDetail(userId, relationshipId);
        return ApiResponse.success(ResponseMessage.RELATIONSHIP_FOUND, response);
    }

    /**
     * 관계 정보 수정
     */
    @PatchMapping("/{relationshipId}")
    @Operation(summary = "관계 정보 수정", description = "등록한 관계의 이름 또는 유형을 수정합니다.")
    public ApiResponse<RelationshipResponse> updateRelationship(
            @Parameter(description = "관계 ID", example = "1", required = true)
            @PathVariable Integer relationshipId,

            @Valid @RequestBody UpdateRelationshipRequest request
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        RelationshipResponse response = relationshipService.updateRelationship(userId, relationshipId, request);
        return ApiResponse.success(ResponseMessage.RELATIONSHIP_UPDATED, response);
    }

    /**
     * 관계 삭제
     */
    @DeleteMapping("/{relationshipId}")
    @Operation(summary = "관계 삭제", description = "등록한 관계를 삭제합니다. (양쪽 모두 해제)")
    public ApiResponse<String> deleteRelationship(
            @Parameter(description = "관계 ID", example = "1", required = true)
            @PathVariable Integer relationshipId
    ) {
        Integer userId = SecurityUtil.getCurrentUserId();
        relationshipService.deleteRelationship(userId, relationshipId);
        return ApiResponse.success(ResponseMessage.RELATIONSHIP_DELETED);
    }
}
