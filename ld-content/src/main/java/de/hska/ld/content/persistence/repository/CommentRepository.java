package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Comment;
import org.springframework.data.repository.CrudRepository;

public interface CommentRepository extends CrudRepository<Comment, Long> {
}
