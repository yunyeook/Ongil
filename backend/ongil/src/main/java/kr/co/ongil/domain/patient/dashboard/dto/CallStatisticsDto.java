package kr.co.ongil.domain.patient.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CallStatisticsDto {
    private Long patientId;
    private Long callCount;
}
