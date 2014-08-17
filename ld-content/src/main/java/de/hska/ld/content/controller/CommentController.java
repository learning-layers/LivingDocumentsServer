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
     * <b>Path:</b> GET {@value de.hska.ld.content.util.Content#RESOURCE_DOCUMENT}
     * </pre>
     *
     * @param commentId     the comment ID
     * @param pageNumber    the page number as a request parameter (default: 0)
     * @param pageSize      the page size as a request parameter (default: 10)
     * @param sortDirection the sort direction as a request parameter (default: 'DESC')
     * @param sortProperty  the sort property as a request parameter (default: 'createdAt')
     * @return <b>200 OK</b> and a document page or <br>
     * <b>404 Not Found</b> if no documents exists or <br>
     * <b>403 Forbidden</b> if authorization failed
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
     * This resource allows it to create a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/document
     * </pre>
     *
     * @param commentId Contains title and optional description of the new document. Example:
     *                  {title: 'New Document', description: '&lt;optional&gt;'}
     * @return <b>200 OK</b> with the generated document<br>
     * <b>400 Bad Request</b> if no title exists<br>
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
     *     <b>Path:</b> PUT {@value Content#RESOURCE_DOCUMENT}/comment
     * </pre>
     *
     * @param comment the content that contains the changes to this comment. Example:<br>
     *                <tt>{text: 'The comment text'}</tt>
     * @return <b>200 OK</b> if the changes have been successfully applied<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<CommentDto> updateComment(@RequestBody Comment comment) {
        comment = commentService.update(comment);
        return new ResponseEntity<>(new CommentDto(comment), HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{commentId}")
    public ResponseEntity<CommentDto> removeComment(@PathVariable Long commentId) {
        commentService.markAsDeleted(commentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
