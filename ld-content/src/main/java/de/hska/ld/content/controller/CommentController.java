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

package de.hska.ld.content.controller;

import de.hska.ld.content.dto.CommentDto;
import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.service.CommentService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p><b>Resource:</b> {@value de.hska.ld.content.util.Content#RESOURCE_COMMENT}
 */
@RestController
@RequestMapping(Content.RESOURCE_COMMENT)
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * <pre>
     * Gets a page of comments.
     *
     * <b>Required roles:</b> ROLE_USER
     * <b>Path:</b> GET /api/comments/{commentId}/comment?page-number=0&amp;page-size=10&amp;sort-direction=DESC&amp;sort-property=createdAt
     * </pre>
     *
     * @param commentId the comment ID
     * @param pageNumber the page number as a request parameter (default: 0)
     * @param pageSize the page size as a request parameter (default: 10)
     * @param sortDirection the sort direction as a request parameter (default: 'DESC')
     * @param sortProperty the sort property as a request parameter (default: 'createdAt')
     * @return the requested subcomments page
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{commentId}/comment")
    public ResponseEntity<Page<Comment>> getCommentsPage(@PathVariable Long commentId,
                                                         @RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                                         @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                                         @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                                         @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        Page<Comment> commentsPage = commentService.getCommentCommentsPage(commentId, pageNumber, pageSize, sortDirection, sortProperty);
        if (commentsPage != null) {
            return new ResponseEntity<>(commentsPage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <pre>
     * Gets a complete list of comments instead of paging through them.
     *
     * <b>Required roles:</b> ROLE_USER
     * <b>Path:</b> GET /api/comments/{commentId}/comment/list
     * </pre>
     *
     * @param commentId the comment id for which one wants the subcomment list
     * @return subcomments list
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{commentId}/comment/list")
    public ResponseEntity<List<Comment>> getCommentsList(@PathVariable Long commentId) {
        List<Comment> commentsPage = commentService.getCommentCommentsList(commentId);
        if (commentsPage != null) {
            return new ResponseEntity<>(commentsPage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * <pre>
     * Agree (Like) with a comment
     *
     * <b>Required roles:</b> ROLE_USER
     * <b>Path:</b> PUT /api/comments/{commentId}/agree
     * </pre>
     *
     * @param commentId the comment that one wants to agree to
     * @return the comment to which was agreed
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/{commentId}/agree")
    public ResponseEntity<Comment> agreeToComment(@PathVariable Long commentId) {
        Comment comment = commentService.agreeToComment(commentId);
        if (comment != null) {
            return new ResponseEntity<>(comment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    /**
     * This resource allows it to create a comment.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/comments/{commentId}/comment
     * </pre>
     *
     * @param commentId the comment id for which one wants to add a subcomment<br>
     *                  {text: '&lt;CommentText&gt;'}
     * @return return the comment plus the parentId of the parent comment
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{commentId}/comment")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long commentId, @RequestBody Comment comment) {
        comment = commentService.replyToComment(commentId, comment);
        return new ResponseEntity<>(new CommentDto(comment), HttpStatus.CREATED);
    }

    /**
     * Updates an existing comment.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/comments
     * </pre>
     *
     * @param comment the updated comment values. Example:<br>
     *                <tt>{text: 'The comment text'}</tt>
     * @return return the comment plus the parentId of the parent comment
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<CommentDto> updateComment(@RequestBody Comment comment) {
        comment = commentService.update(comment);
        return new ResponseEntity<>(new CommentDto(comment), HttpStatus.OK);
    }

    /**
     * Marks an existing comment as deleted.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> DELETE /api/comments/{commentId}
     * </pre>
     *
     * @param commentId the comment id of the comment which shall be marked as deleted
     * @return 200 HTTP.OK
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{commentId}")
    public ResponseEntity<CommentDto> removeComment(@PathVariable Long commentId) {
        commentService.markAsDeleted(commentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
