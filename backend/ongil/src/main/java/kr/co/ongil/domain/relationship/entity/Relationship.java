package kr.co.ongil.domain.relationship.entity;

import jakarta.persistence.*;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.global.common.entity.BaseEntity;
import lombok.*;

@Entity
@Table(name = "relationship")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Relationship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", nullable = false)
    private User guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_set_by_guardian", length = 20)
    private RelationshipType typeSetByGuardian;

    @Column(name = "name_set_by_guardian", length = 30)
    private String nameSetByGuardian;

    @Column(name = "order_set_by_guardian")
    private Integer orderSetByGuardian;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_set_by_patient", length = 20)
    private RelationshipType typeSetByPatient;

    @Column(name = "name_set_by_patient", length = 30)
    private String nameSetByPatient;

    @Column(name = "order_set_by_patient")
    private Integer orderSetByPatient;

    @Builder.Default
    @Column(name = "is_default_for_guardian", nullable = false)
    private boolean isDefaultForGuardian = false;

    @Builder.Default
    @Column(name = "is_default_for_patient", nullable = false)
    private boolean isDefaultForPatient = false;

    @Builder.Default
    @Column(name = "first_alarm", nullable = false)
    private boolean firstAlarm = true;

    @Builder.Default
    @Column(name = "second_alarm", nullable = false)
    private boolean secondAlarm = false;

    @Builder.Default
    @Column(name = "third_alarm", nullable = false)
    private boolean thirdAlarm = false;

    // 비즈니스 메서드
    public void updateRelationshipInfo(User requestUser, String relationshipName, RelationshipType relationshipType) {
        if (requestUser.equals(guardian)) {
            if (relationshipName != null) {
                this.nameSetByGuardian = relationshipName;
            }
            if (relationshipType != null) {
                this.typeSetByGuardian = relationshipType;
            }
        } else if (requestUser.equals(patient)) {
            if (relationshipName != null) {
                this.nameSetByPatient = relationshipName;
            }
            if (relationshipType != null) {
                this.typeSetByPatient = relationshipType;
            }
        }
    }

    /**
     * 정렬 순서 설정
     */
    public void setDisplayOrder(User requestUser, Integer displayOrder) {
        if (requestUser.equals(guardian)) {
            this.orderSetByGuardian = displayOrder;
        } else if (requestUser.equals(patient)) {
            this.orderSetByPatient = displayOrder;
        }
    }

    /**
     * 정렬 순서 조회
     */
    public Integer getDisplayOrder(User requestUser) {
        if (requestUser.equals(guardian)) {
            return orderSetByGuardian;
        } else if (requestUser.equals(patient)) {
            return orderSetByPatient;
        }
        return null;
    }

    public boolean isRelatedUser(User user) {
        return guardian.equals(user) || patient.equals(user);
    }

    public User getCounterpartUser(User user) {
        if (guardian.equals(user)) {
            return patient;
        } else if (patient.equals(user)) {
            return guardian;
        }
        return null;
    }

    /**
     * 대표(기본) 관계 설정
     */
    public void setDefault(User requestUser, boolean isDefault) {
        if (requestUser.equals(guardian)) {
            this.isDefaultForGuardian = isDefault;
        } else if (requestUser.equals(patient)) {
            this.isDefaultForPatient = isDefault;
        }
    }

    /**
     * 대표(기본) 관계 여부 조회
     */
    public boolean isDefault(User requestUser) {
        if (requestUser.equals(guardian)) {
            return isDefaultForGuardian;
        } else if (requestUser.equals(patient)) {
            return isDefaultForPatient;
        }
        return false;
    }
}
