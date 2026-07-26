package site.persistent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.persistent.entity.PasswordResetToken;
import site.persistent.entity.User;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
}
