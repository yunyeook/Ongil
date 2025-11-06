package kr.co.ongil.domain.relationship.repository;

import java.util.List;
import java.util.Optional;
import kr.co.ongil.domain.relationship.entity.Relationship;
import kr.co.ongil.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Integer> {

    @Query("SELECT r.guardian FROM Relationship r WHERE r.patient.id = :patientId")
    List<User> findGuardiansByPatientId(@Param("patientId") Integer patientId);

    @Query("SELECT r.patient FROM Relationship r WHERE r.guardian.id = :guardianId")
    Optional<User> findPatientByGuardianId(@Param("guardianId") Integer guardianId);

    /**
     * 특정 사용자가 보호자 또는 환자로 등록된 모든 관계 조회
     */
    @Query("SELECT r FROM Relationship r WHERE r.guardian = :user OR r.patient = :user")
    List<Relationship> findByGuardianOrPatient(@Param("user") User user);

    /**
     * 두 사용자 간 관계가 이미 존재하는지 확인
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Relationship r " +
            "WHERE (r.guardian = :user1 AND r.patient = :user2) OR (r.guardian = :user2 AND r.patient = :user1)")
    boolean existsByGuardianAndPatient(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * 특정 관계 ID와 사용자로 관계 조회 (권한 검증용)
     */
    @Query("SELECT r FROM Relationship r WHERE r.id = :relationshipId AND (r.guardian = :user OR r.patient = :user)")
    Optional<Relationship> findByIdAndUser(@Param("relationshipId") Integer relationshipId, @Param("user") User user);

    /**
     * 두 사용자 ID로 관계 조회 (guardian-patient 또는 patient-guardian 관계 모두 조회)
     */
    @Query("SELECT r FROM Relationship r WHERE " +
        "(r.guardian.id = :userId1 AND r.patient.id = :userId2) OR " +
        "(r.guardian.id = :userId2 AND r.patient.id = :userId1)")
    Optional<Relationship> findByTwoUserIds(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

}