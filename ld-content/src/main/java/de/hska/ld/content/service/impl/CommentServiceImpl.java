/**
 * Code contributed to the Learning Layers project
 * http://www.learning-layers.eu
 * Development is partly funded by the FP7 Programme of the European
 * Commission under Grant Agreement FP7-ICT-318209.
 * Copyright (c) 2014, Karlsruhe University of Applied Sciences.
 * For a list of contributors see the AUTHORS file at the top-level directory
 * of this distribution.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.util.List;
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
    public List<Comment> getCommentCommentsList(Long commentId) {
        Comment comment = findById(commentId);
        loadContentCollection(comment, Comment.class);
        return comment.getCommentList();
    }

    @Override
    @Transactional
    public Comment save(Comment comment) {
        Comment dbComment = findById(comment.getId());
        User currentUser = Core.currentUser();
        if (dbComment != null) {
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
    public Comment update(Comment comment) {
        Comment dbComment = findById(comment.getId());
        if (comment.getId() == null || dbComment == null) {
            throw new ValidationException("id");
        }
        dbComment.setText(comment.getText());
        return super.save(dbComment);
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
