package kr.co.ongil.domain.patient.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbnormalStatisticsDto {
    private Integer patientId;
    private String safezoneExitByLevel;  // JSON을 Map으로
    private Integer wanderCount;
    private Integer pathCount;
}