package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.repository.CommentRepository;
import de.hska.ld.content.persistence.repository.DocumentRepository;
import de.hska.ld.content.service.CommentService;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class CommentServiceImpl extends AbstractContentService<Comment> implements CommentService {

    @Autowired
    private CommentRepository repository;

    public Comment save(Comment comment) {
        Comment dbComment = findById(comment.getId());
        User currentUser = Core.currentUser();
        if (dbComment == null) {
            comment.setCreator(currentUser);
            comment.setCreatedAt(new Date());
        } else {
            boolean isCreator = dbComment.getCreator().getId().equals(currentUser.getId());
            if (!isCreator) {
                throw new UserNotAuthorizedException();
            }
            dbComment.setModifiedAt(new Date());
            dbComment.setText(comment.getText());
            comment = dbComment;
        }
        return super.save(comment);
    }

    @Override
    public CommentRepository getRepository() {
        return repository;
    }
}
