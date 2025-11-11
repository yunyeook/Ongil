package kr.co.ongil.domain.patient.sos.repository;

import kr.co.ongil.domain.call.entity.CallType;
import kr.co.ongil.domain.patient.dashboard.dto.CallStatisticsDto;
import kr.co.ongil.domain.patient.dashboard.dto.SosStatisticsDto;
import kr.co.ongil.domain.patient.sos.entity.Sos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SosRepository extends JpaRepository<Sos, Integer> {
    @Query("SELECT new kr.co.ongil.domain.patient.dashboard.dto.SosStatisticsDto(" +
            "CAST(s.patient.id AS Long), COUNT(s)) " +
            "FROM Sos s " +
            "WHERE s.createdAt >= :startDate " +
            "GROUP BY s.patient")
    List<SosStatisticsDto> findSosStatisticsByUser(@Param("startDate") LocalDateTime startDate);
}