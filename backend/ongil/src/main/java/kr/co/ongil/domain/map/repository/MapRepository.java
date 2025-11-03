package kr.co.ongil.domain.map.repository;

import kr.co.ongil.domain.map.entity.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapRepository extends JpaRepository<Map,Integer> {

}