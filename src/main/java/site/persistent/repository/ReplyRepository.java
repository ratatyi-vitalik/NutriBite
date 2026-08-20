package site.persistent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.persistent.entity.comments.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
}
