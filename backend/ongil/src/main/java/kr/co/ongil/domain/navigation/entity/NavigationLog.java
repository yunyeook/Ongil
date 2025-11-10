package kr.co.ongil.domain.navigation.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.map.dto.response.RouteResponse;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.*;

import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "navigation_logs", indexes = {
    @Index(name = "idx_patient_id_started_at", columnList = "patient_id, started_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NavigationLog extends BaseEntity {

    @Column(nullable = false)
    private Integer patientId;

    // 출발지
    @Column(nullable = false, length = 100)
    private String startLocationName;

    @Column(nullable = false)
    private Double startLatitude;

    @Column(nullable = false)
    private Double startLongitude;

    // 목적지
    @Column(nullable = false, length = 100)
    private String endLocationName;

    @Column(nullable = false)
    private Double endLatitude;

    @Column(nullable = false)
    private Double endLongitude;

    // 시간
    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    // 시작 유형
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InitiatedBy initiatedBy;

    // 정상 종료 여부
    @Column(nullable = false)
    @Builder.Default
    private Boolean isSuccessful = false;

    // 길안내 로그 생성
    public static NavigationLog of(
        Integer patientId,
        RouteResponse route,
        LocalDateTime startedAt,
        String initiatedBy
    ) {
        return NavigationLog.builder()
            .patientId(patientId)
            .startLocationName(route.startLocation().name())
            .startLatitude(route.startLocation().latitude())
            .startLongitude(route.startLocation().longitude())
            .endLocationName(route.endLocation().name())
            .endLatitude(route.endLocation().latitude())
            .endLongitude(route.endLocation().longitude())
            .startedAt(startedAt)
            .initiatedBy(InitiatedBy.valueOf(initiatedBy))
            .isSuccessful(Boolean.FALSE)
            .build();
    }

    // 길안내 완료 처리
    public void complete(LocalDateTime endedAt, Boolean isSuccessful) {
        this.endedAt = endedAt;
        this.isSuccessful = isSuccessful;
    }
}