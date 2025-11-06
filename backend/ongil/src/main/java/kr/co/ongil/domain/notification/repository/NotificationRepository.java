package kr.co.ongil.domain.notification.repository;

import java.util.List;
import java.util.Optional;
import kr.co.ongil.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    Optional<Notification> findByIdAndUserId(Integer id, Integer userId);

    Page<Notification> findByUserId(Integer userId, Pageable pageable);
    List<Notification> findByUserId(Integer userId);

    Page<Notification> findByUserIdAndIsRead(Integer userId, Boolean isRead, Pageable pageable);
}