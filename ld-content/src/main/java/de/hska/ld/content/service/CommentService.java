package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import org.springframework.data.domain.Page;

public interface CommentService extends ContentService<Comment> {

    Page<Comment> getDocumentCommentsPage(Document document, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

    Comment replyToComment(Long id, Comment comment);

    Page<Comment> getCommentCommentsPage(Comment comment, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty);

}
