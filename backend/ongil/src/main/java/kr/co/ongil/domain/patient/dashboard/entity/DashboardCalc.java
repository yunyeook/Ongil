package kr.co.ongil.domain.patient.dashboard.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "dashboard_calc")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DashboardCalc extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    private Long routeLost;

    private Long safezoneEmer;

    private Long emerCall;

    private Long sosSign;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "safezone_exit", columnDefinition = "jsonb")
    private String safezoneExit;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "favorite", columnDefinition = "jsonb")
    private String favorite;
}
