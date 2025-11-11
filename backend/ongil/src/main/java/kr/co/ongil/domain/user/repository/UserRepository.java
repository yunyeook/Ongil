package kr.co.ongil.domain.user.repository;

import java.util.List;
import kr.co.ongil.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    @org.springframework.data.jpa.repository.Query(
        "select u.phoneNumber from User u where u.id = :id"
    )
    Optional<String> findPhoneNumberById(@org.springframework.data.repository.query.Param("id") Integer id);
}