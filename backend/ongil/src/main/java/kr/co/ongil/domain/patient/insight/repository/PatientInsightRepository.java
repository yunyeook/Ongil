package kr.co.ongil.domain.patient.insight.repository;

import kr.co.ongil.domain.patient.insight.entity.PatientInsight;
import kr.co.ongil.domain.patient.insight.entity.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 환자 인사이트 레포지토리
 */
public interface PatientInsightRepository extends JpaRepository<PatientInsight, Integer> {

    /**
     * 환자의 특정 타입 최신 인사이트 조회
     */
    Optional<PatientInsight> findFirstByPatientIdAndPeriodTypeOrderByPeriodEndDateDesc(
        Integer patientId,
        PeriodType periodType
    );

    /**
     * 환자의 특정 기간 인사이트 조회
     */
    Optional<PatientInsight> findByPatientIdAndPeriodTypeAndPeriodStartDateAndPeriodEndDate(
        Integer patientId,
        PeriodType periodType,
        LocalDate periodStartDate,
        LocalDate periodEndDate
    );

    /**
     * 환자의 특정 타입 인사이트 목록 조회 (최신순)
     */
    List<PatientInsight> findByPatientIdAndPeriodTypeOrderByPeriodEndDateDesc(
        Integer patientId,
        PeriodType periodType
    );

    /**
     * 환자의 특정 타입, 특정 기간 인사이트 조회
     */
    List<PatientInsight> findByPatientIdAndPeriodTypeAndPeriodEndDateBetweenOrderByPeriodEndDateDesc(
        Integer patientId,
        PeriodType periodType,
        LocalDate from,
        LocalDate to
    );

    /**
     * 환자의 모든 타입 최신 인사이트 조회
     */
    List<PatientInsight> findByPatientIdOrderByPeriodEndDateDesc(Integer patientId);
}
