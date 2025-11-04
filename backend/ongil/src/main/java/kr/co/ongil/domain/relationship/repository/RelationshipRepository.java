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
public interface RelationshipRepository extends JpaRepository<Relationship,Integer> {
    @Query("SELECT r.guardian FROM Relationship r WHERE r.patient.id = :patientId")
    List<User> findGuardiansByPatientId(@Param("patientId") Integer patientId);

    @Query("SELECT r.patient FROM Relationship r WHERE r.guardian.id = :guardianId")
    Optional<User> findPatientByGuardianId(@Param("guardianId") Integer guardianId);

}