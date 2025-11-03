package kr.co.ongil.domain.patient.favorite.repository;

import kr.co.ongil.domain.patient.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    List<Favorite> findAllByPatientIdOrderByCountDesc(Integer patientId);

    boolean existsByPatientIdAndLatitudeAndLongitudeAndPlaceName(
        Integer patientId, Double latitude, Double longitude, String placeName);

    Optional<Favorite> findByPatientIdAndIsDefaultTrue(Integer patientId);

    Optional<Favorite> findByIdAndPatientId(Integer id, Integer patientId);

    List<Favorite> findAllByPatientId(Integer patientId);
}