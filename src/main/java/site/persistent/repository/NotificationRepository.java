package site.persistent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.persistent.entity.Notification;
import site.persistent.entity.User;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByUserIdAndIsReadFalse(Long userId);

    long countByUserId(Long userId);

    List<Notification> findTop5ByUserIdAndIsReadTrueOrderByCreatedAtDesc(Long userId);

    List<Notification> findTop5ByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    void deleteByUserAndReplyId(User user, long replyId);

    void deleteByUserAndCommentId(User user, long commentId);
}
