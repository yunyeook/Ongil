package kr.co.ongil.domain.call.repository;

import kr.co.ongil.domain.call.entity.Call;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CallRepository extends JpaRepository<Call,Integer> {

}
