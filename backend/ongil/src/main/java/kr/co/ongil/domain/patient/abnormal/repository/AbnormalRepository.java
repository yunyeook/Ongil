package kr.co.ongil.domain.patient.abnormal.repository;

import kr.co.ongil.domain.patient.abnormal.entity.Abnormal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbnormalRepository extends JpaRepository<Abnormal,Integer> {

}