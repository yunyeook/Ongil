package kr.co.ongil.domain.patient.favorite.repository;

import jakarta.persistence.LockModeType;
import kr.co.ongil.domain.patient.dashboard.dto.FavoriteStatisticsDto;
import kr.co.ongil.domain.patient.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    List<Favorite> findAllByPatientIdOrderByCountDesc(Integer patientId);

    boolean existsByPatientIdAndLatitudeAndLongitudeAndPlaceName(
            Integer patientId, Double latitude, Double longitude, String placeName);

    boolean existsByPatientId(Integer patientId);

    Optional<Favorite> findByPatientIdAndIsDefaultTrue(Integer patientId);

    Optional<Favorite> findByIdAndPatientId(Integer id, Integer patientId);

    List<Favorite> findAllByPatientId(Integer patientId);

    /**
     * 환자의 가장 큰 정렬 순서 조회 (새 즐겨찾기 등록 시 사용)
     */
    @Query("SELECT COALESCE(MAX(f.displayOrder), 0) FROM Favorite f WHERE f.patient.id = :patientId")
    Integer findMaxOrderByPatientId(@Param("patientId") Integer patientId);

    /**
     * 환자의 즐겨찾기 목록 조회 (정렬 순서로 정렬, null은 맨 뒤)
     */
    @Query("SELECT f FROM Favorite f WHERE f.patient.id = :patientId " +
            "ORDER BY CASE WHEN f.displayOrder IS NULL THEN 1 ELSE 0 END, f.displayOrder ASC")
    List<Favorite> findAllByPatientIdOrderByDisplayOrder(@Param("patientId") Integer patientId);

    /**
     * 환자의 즐겨찾기 목록 조회 with Pessimistic Lock (동시성 제어)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM Favorite f WHERE f.patient.id = :patientId")
    List<Favorite> findAllByPatientIdWithLock(@Param("patientId") Integer patientId);

    /**
     * 환자의 모든 즐겨찾기의 default 해제 (isDefault를 false로 설정)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Favorite f SET f.isDefault = false WHERE f.patient.id = :patientId")
    void clearAllDefaultsByPatientId(@Param("patientId") Integer patientId);

    @Query(value = """
            SELECT
                f.patient_id as patientId,
                jsonb_agg(
                    jsonb_build_object(
                        'rank', f.rank,
                        'placeName', f.place_name,
                        'placeCount', f.count
                    ) ORDER BY f.rank
                ) as favorites
            FROM (
                SELECT
                    patient_id,
                    place_name,
                    count,
                    ROW_NUMBER() OVER (PARTITION BY patient_id ORDER BY count DESC) as rank
                FROM favorite
                WHERE created_at >= :startDate
            ) f
            WHERE f.rank <= 3
            GROUP BY f.patient_id
            """, nativeQuery = true)
    List<FavoriteStatisticsDto> getFavoriteStatistics(@Param("startDate") LocalDateTime startDate);

    @Query(value = """
            SELECT f.place_name
            FROM favorite f
            WHERE f.patient_id = :patientId
              AND f.created_at >= :weekStart
            GROUP BY f.place_name
            ORDER BY SUM(f.count) DESC
            LIMIT 1
            """, nativeQuery = true)
    String countThisWeek(@Param("patientId") Integer patientId,
            @Param("weekStart") LocalDateTime weekStart);

    /**
     * 특정 순서보다 큰 즐겨찾기들의 순서를 1씩 감소 (삭제 후 재정렬용)
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Favorite f SET f.displayOrder = f.displayOrder - 1 " +
            "WHERE f.patient.id = :patientId AND f.displayOrder > :deletedOrder")
    void decreaseOrderGreaterThan(@Param("patientId") Integer patientId, @Param("deletedOrder") Integer deletedOrder);

    /**
     * 특정 즐겨찾기만 default로 설정하고 나머지는 해제
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Favorite f SET f.isDefault = (CASE WHEN f.id = :favoriteId THEN true ELSE false END) " +
            "WHERE f.patient.id = :patientId")
    void updateDefaultFavorite(@Param("patientId") Integer patientId, @Param("favoriteId") Integer favoriteId);

    /**
     * 환자의 즐겨찾기 중 DisplayOrder가 가장 높은 즐겨찾기 조회
     */
    Optional<Favorite> findFirstByPatientIdOrderByDisplayOrder(Integer patientId);

}