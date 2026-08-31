package site.persistent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.persistent.entity.Notification;
import site.persistent.entity.User;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    long countByUserIdAndIsReadFalse(Long userId);

    long countByUserId(Long userId);

    List<Notification> findTop5ByUserIdAndIsReadTrueOrderByCreatedAtDesc(Long userId);

    List<Notification> findTop5ByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.isRead ASC, n.createdAt DESC")
    Page<Notification> findPriorityNotificationsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    void deleteByUserAndReplyId(User user, long replyId);

    void deleteByUserAndCommentId(User user, long commentId);
}
