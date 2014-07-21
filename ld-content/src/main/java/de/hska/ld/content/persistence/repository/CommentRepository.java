package de.hska.ld.content.persistence.repository;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Content;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.core.persistence.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends CrudRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c LEFT JOIN document d LEFT JOIN d.commentList dcl LEFT JOIN d.accessList al WHERE al.user = :user WHERE d.id = :documentId AND dcl.id = c.id")
    Page<Comment> findAll(@Param("documentId") Long documentId, @Param("user") User user, Pageable pageable);
}
