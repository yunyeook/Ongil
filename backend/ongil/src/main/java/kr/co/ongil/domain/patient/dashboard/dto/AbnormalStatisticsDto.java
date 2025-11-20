package kr.co.ongil.domain.patient.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public interface AbnormalStatisticsDto {
    Long getPatientId();
    String getSafezoneExitByLevel();  // JSONB를 String으로
    Long getWanderCount();
    Long getPathCount();
}