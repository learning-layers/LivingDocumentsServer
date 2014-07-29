package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Comment;
import org.springframework.data.domain.Page;

public interface CommentService extends ContentService<Comment> {

    Page<Comment> getDocumentCommentsPage(Long documentId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    Comment update(Comment comment);

    Comment replyToComment(Long id, Comment comment);

    Page<Comment> getCommentCommentsPage(Long commentId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

}
