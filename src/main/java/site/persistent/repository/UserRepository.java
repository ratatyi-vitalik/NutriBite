package site.persistent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.persistent.entity.PasswordResetToken;
import site.persistent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByName(String name);

    Page<User> findByBanNotNull(Pageable pageable);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.saved WHERE u.id = :id")
    Optional<User> findUserWithSavedById(@Param("id") Long id);
}
