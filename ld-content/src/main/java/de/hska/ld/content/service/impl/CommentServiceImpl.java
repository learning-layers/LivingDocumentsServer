package de.hska.ld.content.service.impl;

import de.hska.ld.content.persistence.domain.Access;
import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.repository.CommentRepository;
import de.hska.ld.content.service.CommentService;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;

public class CommentServiceImpl extends AbstractContentService<Comment> implements CommentService {

    @Autowired
    private CommentRepository repository;

    @Autowired
    private DocumentService documentService;

    @Override
    public Page<Comment> getDocumentCommentsPage(Long documentId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Document document = documentService.findById(documentId);
        if (document == null) {
            throw new NotFoundException("id");
        }
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        User user = Core.currentUser();
        if (!document.getCreator().equals(user)) {
            try {
                Optional<Access> access = document.getAccessList().stream().filter(ele -> ele.getUser().equals(user)).findFirst();
                access.get();
            } catch (NoSuchElementException e) {
                throw new UserNotAuthorizedException();
            }
        }
        Page<Comment> commentPage = repository.findAllForDocument(document.getId(), pageable);
        return commentPage;
    }

    @Override
    public Page<Comment> getCommentCommentsPage(Long commentId, Integer pageNumber, Integer pageSize, String sortDirection, String sortProperty) {
        Comment comment = findById(commentId);
        if (comment == null) {
            throw new ValidationException("id");
        }
        Sort.Direction direction;
        if (Sort.Direction.ASC.toString().equals(sortDirection)) {
            direction = Sort.Direction.ASC;
        } else {
            direction = Sort.Direction.DESC;
        }
        Pageable pageable = new PageRequest(pageNumber, pageSize, direction, sortProperty);
        Page<Comment> commentPage = repository.findAllForComment(comment.getId(), pageable);
        return commentPage;
    }

    @Override
    @Transactional
    public Comment save(Comment comment) {
        Comment dbComment = findById(comment.getId());
        User currentUser = Core.currentUser();
        if (dbComment == null) {
            //comment.setCreator(currentUser);
            //comment.setCreatedAt(new Date());
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
    @Transactional
    public Comment replyToComment(Long parentId, Comment comment) {
        // TODO add logic to check whether a user has access to reply to a comment
        Comment dbParentComment = findById(parentId);
        User currentUser = Core.currentUser();
        if (dbParentComment == null) {
            throw new NotFoundException("Parent comment");
        } else {
            dbParentComment.getCommentList().add(comment);
            comment.setModifiedAt(new Date());
            comment.setText(comment.getText());
            comment.setParent(dbParentComment);
            comment.setCreatedAt(new Date());
            comment.setCreator(currentUser);
        }
        super.save(dbParentComment);
        return comment;
    }

    @Override
    public CommentRepository getRepository() {
        return repository;
    }

}
