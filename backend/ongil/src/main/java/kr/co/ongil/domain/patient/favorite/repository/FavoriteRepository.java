package kr.co.ongil.domain.patient.favorite.repository;

import kr.co.ongil.domain.patient.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite,Integer> {

}