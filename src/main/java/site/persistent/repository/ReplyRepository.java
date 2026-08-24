package site.persistent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.persistent.entity.comments.Reply;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    List<Reply> findByHiddenFalse();
}
