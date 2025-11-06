package kr.co.ongil.domain.patient.safezone.repository;

import kr.co.ongil.domain.patient.safezone.entity.SafeZone;
import kr.co.ongil.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SafeZoneRepository extends JpaRepository<SafeZone, Integer> {

    Optional<SafeZone> findByPatient(User patient);

    Optional<SafeZone> findByPatientId(Integer patientId);

    boolean existsByPatient(User patient);

    boolean existsByPatientId(Integer patientId);
}