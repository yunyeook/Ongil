package kr.co.ongil.domain.patient.safezone.repository;

import kr.co.ongil.domain.patient.safezone.entity.SafeZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SafeZoneRepository extends JpaRepository<SafeZone,Integer> {

}