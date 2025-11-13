package kr.co.ongil.domain.patient.sos.repository;

import kr.co.ongil.domain.patient.sos.entity.Sos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SosRepository extends JpaRepository<Sos, Integer> {

}