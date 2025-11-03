package kr.co.ongil.domain.navigation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.co.ongil.domain.map.dto.response.LocationInfo;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "길안내 시작 요청")
public class StartNavigationRequest {

    @NotNull
    @Schema(description = "환자 ID", example = "123")
    private Long patientId;

    @NotNull
    @Schema(description = "출발지 정보")
    private LocationInfo startLocation;

    @NotNull
    @Schema(description = "목적지 정보")
    private LocationInfo endLocation;

    @NotNull
    @Schema(description = "시작 주체", example = "PATIENT", allowableValues = {"PATIENT", "GUARDIAN"})
    private String initiatedBy;

}