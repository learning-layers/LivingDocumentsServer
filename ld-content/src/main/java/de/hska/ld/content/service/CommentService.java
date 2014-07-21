package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Comment;

public interface CommentService extends ContentService<Comment> {

    Comment replyToComment(Long id, Comment comment);

}
