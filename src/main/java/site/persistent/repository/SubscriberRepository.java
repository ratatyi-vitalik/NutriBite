package site.persistent.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.persistent.entity.Subscriber;

import java.util.List;
import java.util.Optional;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    boolean existsByEmail(String email);

    @Modifying
    void deleteByEmail(String email);

    Optional<Subscriber> findByEmail(String email);

    @Modifying
    void deleteByToken(String token);

    boolean existsByToken(String token);

    @Query("SELECT s.email FROM Subscriber s WHERE LOWER(s.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<String> findEmailsByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT s.email FROM Subscriber s")
    List<String> findAllEmails(Pageable pageable);
}
