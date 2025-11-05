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

    @Enumerated(EnumType.STRING)
    @Column(name = "type_set_by_patient", length = 20)
    private RelationshipType typeSetByPatient;

    @Column(name = "name_set_by_patient", length = 30)
    private String nameSetByPatient;

    @Column(name = "first_alarm", nullable = false)
    private boolean firstAlarm = true;

    @Column(name = "second_alarm", nullable = false)
    private boolean secondAlarm = false;

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
}
