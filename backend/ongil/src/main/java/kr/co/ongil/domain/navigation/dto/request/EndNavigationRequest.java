package kr.co.ongil.domain.navigation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
@Schema(name = "EndNavigationRequest",description = "길안내 종료 요청")
public record EndNavigationRequest (

    @NotNull
    @Schema(description = "환자 ID", example = "1")
    Long patientId,

    @NotNull
    @Schema(description = "네비게이션 ID", example = "1")
    Long navigationId,

    @Schema(description = "정상 종료 여부", example = "true")
    Boolean isSuccessful
){}