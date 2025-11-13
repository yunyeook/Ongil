package kr.co.ongil.domain.patient.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "환자 메인보드 정보")
@Setter
@Getter
@Builder
@AllArgsConstructor
public class MainboardResponseDto {
    private String favoriteName;
    private Long safezoneExit;
    private Long routeLost;
}
