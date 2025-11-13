package kr.co.ongil.domain.fcm.repository;


import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import kr.co.ongil.domain.fcm.entity.FcmToken;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    Optional<FcmToken> findByToken(String token);

    List<FcmToken> findAllByUserId(Integer userId);
    void deleteAllByUserId(Integer userId);
    Optional<FcmToken> findByUserId(Integer userId);
}