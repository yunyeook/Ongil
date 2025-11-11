package kr.co.ongil.domain.patient.abnormal.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.patient.abnormal.dto.request.AbnormalCreateRequest;
import kr.co.ongil.domain.patient.safezone.entity.SafeZoneLevel;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "abnormal_logs")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Abnormal extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "abnormal_type", nullable = false)
    private AbnormalType abnormalType;

    @Enumerated(EnumType.STRING)
    @Column(name = "safe_zone_level")
    private SafeZoneLevel safeZoneLevel;  // 안전범위 이탈/배회 시 몇 단계인지

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "center_latitude")
    private Double centerLatitude;  // 안전범위 중심 좌표 (비교용)

    @Column(name = "center_longitude")
    private Double centerLongitude;  // 안전범위 중심 좌표 (비교용)

    @Column(name = "distance_from_center")
    private Double distanceFromCenter;  // 중심으로부터의 거리 (미터)

    @Column(name = "boundary_radius")
    private Double boundaryRadius;  // 해당 단계의 경계 반경 (미터)

    @Column(name = "elapsed_time")
    private Integer elapsedTime;  // 배회 시 경과 시간 (초)

    @Column(name = "threshold_time")
    private Integer thresholdTime;  // 배회 기준 시간 (초)

    public static Abnormal from(AbnormalCreateRequest request, User patient) {
        return Abnormal.builder()
            .patient(patient)
            .abnormalType(request.getAbnormalType())
            .safeZoneLevel(request.getSafeZoneLevel())
            .latitude(request.latitude())
            .longitude(request.longitude())
            .centerLatitude(request.centerLatitude())
            .centerLongitude(request.centerLongitude())
            .distanceFromCenter(request.distanceFromCenter())
            .boundaryRadius(request.boundaryRadius())
            .elapsedTime(request.elapsedTime())
            .thresholdTime(request.thresholdTime())
            .build();
    }

    /**
     * 초를 분으로 변환 (null-safe)
     */
    public static Integer convertSecondsToMinutes(Integer seconds) {
        if (seconds == null) {
            return null;
        }
        return (int) Math.ceil(seconds / 60.0);  // 올림 처리
    }

}