package kr.co.ongil.domain.patient.dashboard.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "dashboard_calc")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardCalc extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    private User patient;

    private Integer routeLost;

    private Integer safezoneEmer;

    private Integer emerCall;

    private Integer sosSign;

    @Column(columnDefinition = "jsonb")
    private String safezoneExit;

    @Column(columnDefinition = "jsonb")
    private String favorite;
}
