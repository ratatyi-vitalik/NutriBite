package site.persistent.repository;

import site.persistent.entity.comments.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.persistent.entity.comments.Reply;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Reply> findByHiddenFalse();
}
