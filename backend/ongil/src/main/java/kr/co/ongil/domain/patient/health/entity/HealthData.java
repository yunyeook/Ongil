package kr.co.ongil.domain.patient.health.entity;

import jakarta.persistence.*;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 환자 건강 데이터 엔티티
 * Samsung Health SDK에서 수집한 생체 데이터를 저장
 * 모든 타입의 데이터를 공통 포맷(average/max/min)으로 저장
 */
@Entity
@Table(
    name = "health_data",
    indexes = {
        @Index(name = "idx_health_patient_measured_at", columnList = "patient_id, measured_at"),
        @Index(name = "idx_health_patient_type", columnList = "patient_id, type")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_health_patient_type_measured_at",
            columnNames = {"patient_id", "type", "measured_at"}
        )
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class HealthData extends BaseEntity {

    /**
     * 환자 ID
     */
    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    /**
     * 건강 데이터 타입 (심박수, 수면, 산소포화도 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private HealthDataType type;

    /**
     * 평균값
     */
    @Column(nullable = false)
    private Double average;

    /**
     * 최대값
     */
    @Column(nullable = false)
    private Double max;

    /**
     * 최소값
     */
    @Column(nullable = false)
    private Double min;

    /**
     * 단위 (bpm, %, hours, steps 등)
     */
    @Column(nullable = false, length = 20)
    private String unit;

    /**
     * 측정 시각
     * Samsung Health에서 데이터를 수집한 시점
     */
    @Column(name = "measured_at", nullable = false)
    private LocalDateTime measuredAt;

    /**
     * 기존 데이터를 새 데이터로 업데이트 (Upsert 용)
     * 같은 (patient_id, type, measured_at)에 대해 값이 갱신될 때 사용
     *
     * @param newData 새로운 건강 데이터
     */
    public void updateFrom(HealthData newData) {
        this.average = newData.getAverage();
        this.min = newData.getMin();
        this.max = newData.getMax();
        this.unit = newData.getUnit();
        // measuredAt는 유니크 제약의 일부이므로 변경하지 않음
        // patientId, type도 동일하게 유지
    }
}
