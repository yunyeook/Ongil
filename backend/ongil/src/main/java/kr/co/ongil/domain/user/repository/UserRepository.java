package kr.co.ongil.domain.user.repository;

import java.util.List;
import kr.co.ongil.domain.user.entity.User;
import kr.co.ongil.domain.user.entity.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Set<Long> findIdByUserType(UserType userType);

    @Query(
        "select u.phoneNumber from User u where u.id = :id"
    )
    Optional<String> findPhoneNumberById(@org.springframework.data.repository.query.Param("id") Integer id);
}