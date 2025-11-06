package kr.co.ongil.domain.relationship.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import kr.co.ongil.domain.relationship.entity.Relationship;
import kr.co.ongil.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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
     * 보호자의 가장 큰 정렬 순서 조회 (새 관계 등록 시 사용)
     */
    @Query("SELECT COALESCE(MAX(r.orderSetByGuardian), 0) FROM Relationship r WHERE r.guardian = :guardian")
    Integer findMaxOrderByGuardian(@Param("guardian") User guardian);

    /**
     * 환자의 가장 큰 정렬 순서 조회 (새 관계 등록 시 사용)
     */
    @Query("SELECT COALESCE(MAX(r.orderSetByPatient), 0) FROM Relationship r WHERE r.patient = :patient")
    Integer findMaxOrderByPatient(@Param("patient") User patient);

    /**
     * 보호자의 관계 목록 조회 (정렬 순서로 정렬, null은 맨 뒤)
     */
    @Query("SELECT r FROM Relationship r WHERE r.guardian = :guardian " +
            "ORDER BY CASE WHEN r.orderSetByGuardian IS NULL THEN 1 ELSE 0 END, r.orderSetByGuardian ASC")
    List<Relationship> findByGuardianOrderByOrder(@Param("guardian") User guardian);

    /**
     * 환자의 관계 목록 조회 (정렬 순서로 정렬, null은 맨 뒤)
     */
    @Query("SELECT r FROM Relationship r WHERE r.patient = :patient " +
            "ORDER BY CASE WHEN r.orderSetByPatient IS NULL THEN 1 ELSE 0 END, r.orderSetByPatient ASC")
    List<Relationship> findByPatientOrderByOrder(@Param("patient") User patient);

    /**
     * 보호자의 관계 목록 조회 with Pessimistic Lock (동시성 제어)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Relationship r WHERE r.guardian = :guardian")
    List<Relationship> findByGuardianWithLock(@Param("guardian") User guardian);

    /**
     * 환자의 관계 목록 조회 with Pessimistic Lock (동시성 제어)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Relationship r WHERE r.patient = :patient")
    List<Relationship> findByPatientWithLock(@Param("patient") User patient);

    /**
     * 보호자의 대표 관계 해제 (모든 관계의 isDefaultForGuardian을 false로 설정)
     */
    @Modifying
    @Query("UPDATE Relationship r SET r.isDefaultForGuardian = false WHERE r.guardian = :guardian")
    void clearDefaultForGuardian(@Param("guardian") User guardian);

    /**
     * 환자의 대표 관계 해제 (모든 관계의 isDefaultForPatient를 false로 설정)
     */
    @Modifying
    @Query("UPDATE Relationship r SET r.isDefaultForPatient = false WHERE r.patient = :patient")
    void clearDefaultForPatient(@Param("patient") User patient);

    /**
     * 보호자의 대표 관계 조회
     */
    @Query("SELECT r FROM Relationship r WHERE r.guardian = :guardian AND r.isDefaultForGuardian = true")
    Optional<Relationship> findDefaultByGuardian(@Param("guardian") User guardian);

    /**
     * 환자의 대표 관계 조회
     */
    @Query("SELECT r FROM Relationship r WHERE r.patient = :patient AND r.isDefaultForPatient = true")
    Optional<Relationship> findDefaultByPatient(@Param("patient") User patient);

    /**
     * 환자 ID와 보호자 ID로 관계 존재 여부 확인 (권한 검증용)
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Relationship r " +
            "WHERE r.patient.id = :patientId AND r.guardian.id = :guardianId")
    boolean existsByPatientIdAndGuardianId(@Param("patientId") Integer patientId, @Param("guardianId") Integer guardianId);
}