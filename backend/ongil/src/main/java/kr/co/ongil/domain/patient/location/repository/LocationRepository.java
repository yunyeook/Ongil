package kr.co.ongil.domain.patient.location.repository;

import kr.co.ongil.domain.patient.location.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location,Integer> {

}