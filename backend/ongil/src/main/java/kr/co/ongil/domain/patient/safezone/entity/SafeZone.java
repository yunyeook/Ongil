package kr.co.ongil.domain.patient.safezone.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "safe_zones")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SafeZone extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private User patient;

    @Column(name = "first_boundary", nullable = false)
    @Builder.Default
    private Double firstBoundary = 150.0;

    @Column(name = "second_boundary", nullable = false)
    @Builder.Default
    private Double secondBoundary = 500.0;

    @Column(name = "third_boundary", nullable = false)
    @Builder.Default
    private Double thirdBoundary = 1000.0;

    @Column(name = "first_time", nullable = false)
    @Builder.Default
    private Integer firstTime = 60;

    @Column(name = "second_time", nullable = false)
    @Builder.Default
    private Integer secondTime = 30;

    @Column(name = "third_time", nullable = false)
    @Builder.Default
    private Integer thirdTime = 15;

    // 비즈니스 로직 메서드
    public void updateBoundaries(Double firstBoundary, Double secondBoundary, Double thirdBoundary) {
        if (firstBoundary != null) {
            this.firstBoundary = firstBoundary;
        }
        if (secondBoundary != null) {
            this.secondBoundary = secondBoundary;
        }
        if (thirdBoundary != null) {
            this.thirdBoundary = thirdBoundary;
        }
    }

    public void updateTimes(Integer firstTime, Integer secondTime, Integer thirdTime) {
        if (firstTime != null) {
            this.firstTime = firstTime;
        }
        if (secondTime != null) {
            this.secondTime = secondTime;
        }
        if (thirdTime != null) {
            this.thirdTime = thirdTime;
        }
    }

    public void resetToDefault() {
        this.firstBoundary = 150.0;
        this.secondBoundary = 500.0;
        this.thirdBoundary = 1000.0;
        this.firstTime = 60;
        this.secondTime = 30;
        this.thirdTime = 15;
    }
}
