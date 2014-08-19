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

import de.hska.ld.content.dto.BreadcrumbDto;
import de.hska.ld.content.persistence.domain.*;
import de.hska.ld.content.service.CommentService;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.exception.NotFoundException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.util.Core;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * <p><b>Resource:</b> {@value Content#RESOURCE_DOCUMENT}
 */
@RestController
@RequestMapping(Content.RESOURCE_DOCUMENT)
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private CommentService commentService;

    /**
     * <pre>
     * Gets a page of documents.
     *
     * <b>Required roles:</b> ROLE_USER
     * <b>Path:</b> GET {@value de.hska.ld.content.util.Content#RESOURCE_DOCUMENT}
     * </pre>
     *
     * @param pageNumber    the page number as a request parameter (default: 0)
     * @param pageSize      the page size as a request parameter (default: 10)
     * @param sortDirection the sort direction as a request parameter (default: 'DESC')
     * @param sortProperty  the sort property as a request parameter (default: 'createdAt')
     * @return <b>200 OK</b> and a document page or <br>
     * <b>404 Not Found</b> if no documents exists or <br>
     * <b>403 Forbidden</b> if authorization failed
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Page<Document>> getDocumentsPage(@RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                                           @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                                           @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                                           @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        Page<Document> documentsPage = documentService.getDocumentsPage(pageNumber, pageSize, sortDirection, sortProperty);
        if (documentsPage != null) {
            return new ResponseEntity<>(documentsPage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/discussions")
    public ResponseEntity<Page<Document>> getDiscussionsPage(@PathVariable Long documentId,
                                                             @RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                                           @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                                           @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                                           @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        Page<Document> documentsPage = documentService.getDiscussionDocumentsPage(documentId, pageNumber, pageSize, sortDirection, sortProperty);
        if (documentsPage != null) {
            return new ResponseEntity<>(documentsPage, HttpStatus.OK);
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
     * @param document Contains title and optional description of the new document. Example:
     *                 {title: 'New Document', description: '&lt;optional&gt;'}
     * @return <b>200 OK</b> with the generated document<br>
     * <b>400 Bad Request</b> if no title exists<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Document> createDocument(@RequestBody Document document) {
        document = documentService.save(document);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value="/{documentId}/discussion")
    public ResponseEntity<Document> createDiscussion(@PathVariable Long documentId, @RequestBody Document document) {
        document = documentService.addDiscussionToDocument(documentId, document);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/{documentId}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long documentId, @RequestBody Document document, @RequestParam(value = "cmd", defaultValue = "all") String cmd) {
        Document dbDocument = documentService.findById(documentId);
        if (dbDocument.isDeleted()) {
            throw new NotFoundException("id");
        }
        if ("title".equals(cmd)) {
            if (document.getTitle() == null || "".equals(document.getTitle())) {
                throw new ValidationException("title");
            }
            dbDocument.setTitle(document.getTitle());
        } else if ("description".equals(cmd)) {
            if (document.getDescription() == null || "".equals(document.getDescription())) {
                throw new ValidationException("description");
            }
            dbDocument.setDescription(document.getDescription());
        } else {
            throw new ValidationException("command");
        }
        dbDocument = documentService.save(dbDocument);
        return new ResponseEntity<>(dbDocument, HttpStatus.OK);
    }

    /**
     * This resource allows it to read a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/document
     * </pre>
     *
     * @param documentId Contains title and optional description of the new document. Example:
     *                   {title: 'New Document', description: '&lt;optional&gt;'}
     * @return <b>200 OK</b> with the generated document<br>
     * <b>400 Bad Request</b> if no title exists<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}")
    public ResponseEntity<Document> readDocument(@PathVariable Long documentId) {
        Document document = documentService.findById(documentId);
        documentService.loadContentCollection(document, Attachment.class, Comment.class, Tag.class);
        if (document.isDeleted()) {
            throw new NotFoundException("id");
        }
        return new ResponseEntity<>(document, HttpStatus.OK);
    }

    /**
     * Deletes a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> DELETE {@value Content#RESOURCE_DOCUMENT}/document/{documentId}
     * </pre>
     *
     * @param documentId the document id of the document one wants to delete
     * @return <b>200 OK</b> if the removal of the document has been successfully executed<br>
     * <b>404 NOT FOUND</b> if a document with the given id isn't present in this application<br>
     * <b>500 Internal Server Error</b> if there occured any other server side issue
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{documentId}")
    public ResponseEntity removeDocument(@PathVariable Long documentId) {
        documentService.markAsDeleted(documentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Fetches the commments for a specifc document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/document/{documentId}/comments
     * </pre>
     *
     * @param documentId the document id of the document the comments shall be fetched for
     * @return <b>200 OK</b> and a list of comments
     * <b>404 NOT FOUND</b> if there is no document present within the system that has the specified documentId
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/comment")
    public ResponseEntity<Page<Comment>> getCommentsPage(@PathVariable Long documentId,
                                                         @RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                                         @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                                         @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                                         @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        Page<Comment> commentsPage = commentService.getDocumentCommentsPage(documentId, pageNumber, pageSize, sortDirection, sortProperty);
        if (commentsPage != null && commentsPage.getNumberOfElements() > 0) {
            return new ResponseEntity<>(commentsPage, HttpStatus.OK);
        } else {
            throw new NotFoundException();
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
     * @param documentId Contains title and optional description of the new document. Example:
     *                   {title: 'New Document', description: '&lt;optional&gt;'}
     * @return <b>200 OK</b> with the generated document<br>
     * <b>400 Bad Request</b> if no title exists<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{documentId}/comment")
    public ResponseEntity<Comment> addComment(@PathVariable Long documentId, @RequestBody Comment comment) {
        comment = documentService.addComment(documentId, comment);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    /**
     * This resource allows it to create a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/document
     * </pre>
     *
     * @param documentId Contains title and optional description of the new document. Example:
     *                   {title: 'New Document', description: '&lt;optional&gt;'}
     * @return <b>200 OK</b> with the generated document<br>
     * <b>400 Bad Request</b> if no title exists<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{documentId}/tag/{tagId}")
    public ResponseEntity<Document> addTag(@PathVariable Long documentId, @PathVariable Long tagId) {
        documentService.addTag(documentId, tagId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * This resource allows it to create a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/document
     * </pre>
     *
     * @param documentId Contains title and optional description of the new document. Example:
     *                   {title: 'New Document', description: '&lt;optional&gt;'}
     * @return <b>200 OK</b> with the generated document<br>
     * <b>400 Bad Request</b> if no title exists<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{documentId}/tag/{tagId}")
    public ResponseEntity removeTag(@PathVariable Long documentId, @PathVariable Long tagId) {
        documentService.removeTag(documentId, tagId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Fetches the commments for a specifc document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/document/{documentId}/comments
     * </pre>
     *
     * @param documentId the document id of the document the comments shall be fetched for
     * @return <b>200 OK</b> and a list of comments
     * <b>404 NOT FOUND</b> if there is no document present within the system that has the specified documentId
     */
     @Secured(Core.ROLE_USER)
     @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/tags")
     public ResponseEntity<Page<Tag>> getTagsPage(@PathVariable Long documentId,
                                                  @RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
                                                  @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
                                                  @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
                                                  @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        Page<Tag> tagsPage = documentService.getDocumentTagsPage(documentId, pageNumber, pageSize, sortDirection, sortProperty);
        if (tagsPage != null && tagsPage.getNumberOfElements() > 0) {
            return new ResponseEntity<>(tagsPage, HttpStatus.OK);
        } else {
            throw new NotFoundException();
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/tags/list")
    public ResponseEntity<List<Tag>> getAllTags(@PathVariable Long documentId) {
        List<Tag> tagList = documentService.getDocumentTagsList(documentId);
        if (tagList != null) {
            return new ResponseEntity<>(tagList, HttpStatus.OK);
        } else {
            throw new NotFoundException();
        }
    }

    /**
     * This resource allows uploading files.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/upload
     * </pre>
     *
     * @param file       the Multipart file that has been uploaded
     * @param documentId the document ID to which the file shall be attached
     * @return <b>200 OK</b> if the upload has been successfully performed<br>
     * <b>400 BAD REQUEST</b> if empty file parameter<br>
     * <b>500 Internal Server Error</b> if there occurred any other server side issue
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/upload/{attachmentId}")
    public ResponseEntity<Long> uploadFile(@RequestParam MultipartFile file, @PathVariable Long attachmentId, @RequestParam Long documentId) {
        String name = file.getOriginalFilename();
        if (!file.isEmpty()) {
            attachmentId = documentService.updateAttachment(documentId, attachmentId, file, name);
            return new ResponseEntity<>(attachmentId, HttpStatus.OK);
        } else {
            throw new ValidationException("file");
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/upload")
    public ResponseEntity<Long> uploadFile(@RequestParam MultipartFile file, @RequestParam Long documentId) {
        String name = file.getOriginalFilename();
        if (!file.isEmpty()) {
            Long attachmentId = documentService.addAttachment(documentId, file, name);
            return new ResponseEntity<>(attachmentId, HttpStatus.OK);
        } else {
            throw new ValidationException("file");
        }
    }

    /**
     * This resource allows downloading a file attachment.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path - option 1 (downloading an attachment):</b> GET {@value Content#RESOURCE_DOCUMENT}/{documentId}/download?attachment={position}
     *     <b>Path - option 2 (download a main content):</b> GET {@value Content#RESOURCE_DOCUMENT}/{documentId}/download
     * </pre>
     *
     * @param documentId the ID of the document that contains the needed attachment
     * @param position   the position of the attachment in the attachment list
     * @param response   <b>FILE DOWNLOAD INITIATED</b> if the attachment could be found, and the download is starting<br>
     *                   <b>400 BAD REQUEST</b><br>
     *                   <b>403 FORBIDDEN</b> if the access to this attachment has been denied<br>
     *                   <b>404 NOT FOUND</b> if no attachment has been found for the given document ID or attachment position<br>
     *                   <b>500 Internal Server Error</b> if there occurred any other server side issue
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/download")
    public void downloadFile(@PathVariable Long documentId, @RequestParam Integer position, HttpServletResponse response) {
        try {
            Attachment attachment = documentService.getAttachment(documentId, position);
            byte[] source = attachment.getSource();
            InputStream is = new ByteArrayInputStream(source);
            response.setContentType(attachment.getMimeType());
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(is, outputStream);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.GET, value = "/search")
//    public ResponseEntity<List<NodeDto>> searchForDocumentNode(@JcrSession Session session, @RequestParam String query) {
//        try {
//            List<Node> nodeList = jcrService.searchDocumentNode(session, query);
//            if (nodeList == null || nodeList.isEmpty()) {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            } else {
//                List<NodeDto> nodeDtoList = nodeList.stream().map(NodeDto::new).collect(Collectors.toList());
//                return new ResponseEntity<>(nodeDtoList, HttpStatus.OK);
//            }
//        } catch (RepositoryException e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/breadcrumbs")
    public ResponseEntity<List<BreadcrumbDto>> getBreadcrumbs(@PathVariable Long documentId) {
        List<BreadcrumbDto> breadcrumbList = documentService.getBreadcrumbs(documentId);
        if (breadcrumbList != null) {
            return new ResponseEntity<>(breadcrumbList, HttpStatus.OK);
        } else {
            throw new NotFoundException();
        }
    }

    /**
     * This resource allows it to add a subscription for tracking changes to a specific node.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/{nodeId}/subscribe
     * </pre>
     *
     * @param documentId the document id of the document that shall be tracked
     * @param type       the subscription type (e.g. DOCUMENT, ATTACHMENT, COMMENT, DISCUSSION, USER) depending on which
     *                   part of a document the user wants to receive notifications for.
     * @return <b>201 CREATED</b> if a subscription has been successfully applied to the document<br>
     * <b>404 NOT FOUND</b> if the node could not be found within the system
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{documentId}/subscribe")
    public ResponseEntity subscribe(@PathVariable Long documentId, @RequestBody Subscription.Type type) {
        documentService.addSubscription(documentId, type);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@AuthenticationPrincipal User user) {
        List<Notification> notificationList = documentService.getNotifications(user);
        return new ResponseEntity<>(notificationList, HttpStatus.OK);
    }
}