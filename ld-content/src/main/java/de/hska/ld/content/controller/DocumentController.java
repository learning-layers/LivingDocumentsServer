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
import de.hska.ld.core.util.Core;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
     * <b>Path:</b> GET /api/documents?page-number=0&amp;page-size=10&amp;sort-direction=DESC&amp;sort-property=createdAt
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

    /**
     * <pre>
     * Gets a page of discussions (sub documents) of a document.
     *
     * <b>Required roles:</b> ROLE_USER
     * <b>Path:</b> GET /api/documents/{documentId}/discussions?page-number=0&amp;page-size=10&amp;sort-direction=DESC&amp;sort-property=createdAt
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
     *     <b>Path:</b> POST /api/documents
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

    /**
     * This resource allows it to create a discussion (sub document) and append it to a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/documents/{documentId}/discussion
     * </pre>
     *
     * @param documentId the document id of the document one wants to append the discussion to.
     * @param document   Contains title and optional description of the new document. Example:
     *                   {title: 'New Document', description: '&lt;optional&gt;'}
     * @return <b>200 OK</b> with the generated discussion<br>
     * <b>400 Bad Request</b> if no title exists<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{documentId}/discussion")
    public ResponseEntity<Document> createDiscussion(@PathVariable Long documentId, @RequestBody Document document) {
        document = documentService.addDiscussionToDocument(documentId, document);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    /**
     * Updates a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> PUT /api/documents/{documentId}
     * </pre>
     *
     * @param documentId the document id of the document which shall be updated
     * @param document   Contains title and optional description of the new document. Example:
     *                   {title: 'New Document', description: '&lt;optional&gt;'}
     * @param cmd        default=all, describes which section of a document shall be updated
     * @return the updated document
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/{documentId}")
    public ResponseEntity<Document> updateDocument(@PathVariable Long documentId, @RequestBody Document document, @RequestParam(value = "cmd", defaultValue = "all") String cmd) {
        Document dbDocument = documentService.findById(documentId);
        if (dbDocument.isDeleted()) {
            throw new NotFoundException("id");
        }
        switch (cmd) {
            case "title":
                if (document.getTitle() == null || "".equals(document.getTitle())) {
                    throw new ValidationException("title");
                }
                dbDocument.setTitle(document.getTitle());
                break;
            case "description":
                if (document.getDescription() == null || "".equals(document.getDescription())) {
                    throw new ValidationException("description");
                }
                dbDocument.setDescription(document.getDescription());
                break;
            case "all":
                dbDocument.setTitle(document.getTitle());
                dbDocument.setDescription(document.getDescription());
                break;
            default:
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
     *     <b>Path:</b> GET /api/documents/{documentId}
     * </pre>
     *
     * @param documentId The document id of the document one wants to retrieve
     * @return <b>200 OK</b> with the document<br> or <b>404 NOT FOUND</b>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}")
    public ResponseEntity<Document> readDocument(@PathVariable Long documentId) {
        Document document = documentService.findById(documentId);
        documentService.loadContentCollection(document, Attachment.class, Comment.class, Tag.class, Hyperlink.class);
        document.getAttachmentList().remove(0);
        Access access = documentService.getCurrentUserPermissions(documentId, "all");
        if (access != null) {
            document.getAccessList().add(access);
        }
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
     *     <b>Path:</b> DELETE /api/documents/{documentId}
     * </pre>
     *
     * @param documentId the document id of the document one wants to delete
     * @return <b>200 OK</b> if the removal of the document has been successfully executed<br>
     * <b>404 NOT FOUND</b> if a document with the given id isn't present in this application<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{documentId}")
    public ResponseEntity removeDocument(@PathVariable Long documentId) {
        documentService.markAsDeleted(documentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Fetches a page of commments for a specifc document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/document/{documentId}/comment?page-number=0&amp;page-size=10&amp;sort-direction=DESC&amp;sort-property=createdAt
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
     * This resource allows it to create a comment and append it to a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/documents/{documentId}/comment
     * </pre>
     *
     * @param documentId The document id one wants to append the comment to.
     * @param comment    The comment one want to append to a document. Example: <br>
     *                   {text: '&lt;comment text&gt;'}
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
     * This resource allows it to add a tag to a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/documents/{documentId}/tag/{tagId}
     * </pre>
     *
     * @param documentId The document id of the document one want to add the tag to.
     * @param tagId      the tag id of the tag one wants to add to the document.
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
     * This resource allows it to remove a tag of a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> DELETE /api/documents/{documentId}/tag/{tagId}
     * </pre>
     *
     * @param documentId The document id of the document one want to remove the tag from.
     * @param tagId      the tag id of the tag one wants to remove.
     * @return <b>200 OK</b> if successful.
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{documentId}/tag/{tagId}")
    public ResponseEntity removeTag(@PathVariable Long documentId, @PathVariable Long tagId) {
        documentService.removeTag(documentId, tagId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * This resource allows it to create a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/documents/{documentId}/hyperlinks
     * </pre>
     *
     * @param documentId the document id one wants to a hyperlink to.
     * @param hyperlink  the hyperlink that shall be added to the document. Example: <br>
     *                   {url:'&lt;url&gt;', description:'&lt;description&gt;'}
     * @return <b>200 OK</b> if successful.
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{documentId}/hyperlinks")
    public ResponseEntity<Document> addHyperlink(@PathVariable Long documentId, @RequestBody Hyperlink hyperlink) {
        documentService.addHyperlink(documentId, hyperlink);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Fetches a tags page for a specifc document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/documents/{documentId}/tags?page-number=0&amp;page-size=10&amp;sort-direction=DESC&amp;sort-property=createdAt
     * </pre>
     *
     * @param documentId the document id of the document the tag shall be fetched for.
     * @return <b>200 OK</b> the requested tags page
     * <b>404 NOT FOUND</b> if there is no tag present
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

    /**
     * Fetches all tags as list for a specific document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/documents/{documentId}/tags/list
     * </pre>
     *
     * @param documentId the document the tags shall be fetched for.
     * @return the tags list of the document.
     */
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
     * This resource allows it to update of a file content.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/documents/upload/{attachmentId}
     * </pre>
     *
     * @param file         the Multipart file that has been uploaded
     * @param documentId   the document ID to which the file shall be attached
     * @param attachmentId the attachment id of the attachment that shall be updated
     * @return <b>200 OK</b> if the upload has been successfully performed<br>
     * <b>400 BAD REQUEST</b> if empty file parameter<br>
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

    /**
     * This resource allows it upload a file and attach it to a document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/documents/upload?documentId=1
     * </pre>
     *
     * @param file       the Multipart file that has been uploaded
     * @param documentId the document ID to which the file shall be attached
     * @return <b>200 OK</b> if the upload has been successfully performed<br>
     * <b>400 BAD REQUEST</b> if empty file parameter<br>
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/upload")
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
     *     <b>Path - option 1 (downloading an attachment):</b> GET /api/documents/{documentId}/download?attachment={position}
     *     <b>Path - option 2 (download a main content):</b> GET /api/documents/{documentId}/download
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
            String fileName = URLEncoder.encode(attachment.getName(), "UTF-8");
            fileName = URLDecoder.decode(fileName, "ISO8859_1");
            //response.setContentType("application/x-msdownload");
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(is, outputStream);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * This resource allows downloading a file attachment.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path</b> GET /api/documents/{documentId}/download/{attachmentId}
     * </pre>
     *
     * @param documentId   the ID of the document that contains the needed attachment
     * @param attachmentId the attachment id of the attachment that shall be retrieved
     * @param response     <b>FILE DOWNLOAD INITIATED</b> if the attachment could be found, and the download is starting<br>
     *                     <b>400 BAD REQUEST</b><br>
     *                     <b>403 FORBIDDEN</b> if the access to this attachment has been denied<br>
     *                     <b>404 NOT FOUND</b> if no attachment has been found for the given document ID or attachment position<br>
     *                     <b>500 Internal Server Error</b> if there occurred any other server side issue
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/download/{attachmentId}")
    public void downloadFile(@PathVariable Long documentId, @PathVariable Long attachmentId, HttpServletResponse response) {
        try {
            Attachment attachment = documentService.getAttachmentByAttachmentId(documentId, attachmentId);
            byte[] source = attachment.getSource();
            InputStream is = new ByteArrayInputStream(source);
            response.setContentType(attachment.getMimeType());
            String fileName = URLEncoder.encode(attachment.getName(), "UTF-8");
            fileName = URLDecoder.decode(fileName, "ISO8859_1");
            //response.setContentType("application/x-msdownload");
            response.setHeader("Content-disposition", "attachment; filename=" + fileName);
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(is, outputStream);
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Fetches an attachment page for a specifc document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/documents/{documentId}/attachment?attachment-types=image/png&amp;page-number=0&amp;page-size=10&amp;sort-direction=DESC&amp;sort-property=createdAt
     * </pre>
     *
     * @param documentId      the document id of the document the tag shall be fetched for.
     * @param attachmentTypes the mime types of the attachments that shall be retrieved.
     * @return <b>200 OK</b> the requested tags page
     * <b>404 NOT FOUND</b> if there is no attachment with the given mime type present
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/attachment")
    public ResponseEntity<Page<Attachment>> getDocumentAttachmentPage(
            @PathVariable Long documentId,
            @RequestParam(value = "attachment-types", defaultValue = "all") String attachmentTypes,
            @RequestParam(value = "excluded-attachment-types", defaultValue = "") String excludedAttachmentTypes,
            @RequestParam(value = "page-number", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "page-size", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sort-direction", defaultValue = "DESC") String sortDirection,
            @RequestParam(value = "sort-property", defaultValue = "createdAt") String sortProperty) {
        Page<Attachment> attachmentPage = documentService.getDocumentAttachmentPage(documentId, attachmentTypes, excludedAttachmentTypes, pageNumber, pageSize, sortDirection, sortProperty);
        if (attachmentPage != null) {
            return new ResponseEntity<>(attachmentPage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Fetches an attachment list for a specifc document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/documents/{documentId}/attachment/list
     * </pre>
     *
     * @param documentId the document id of the document the attachments shall be fetched for.
     * @return <b>200 OK</b> the requested tags page
     * <b>404 NOT FOUND</b> if there is no attachment with the given mime type present
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/attachment/list")
    public ResponseEntity<List<Attachment>> getDocumentAttachmentList(@PathVariable Long documentId) {
        List<Attachment> attachmentList = documentService.getDocumentAttachmentList(documentId);
        if (attachmentList != null) {
            return new ResponseEntity<>(attachmentList, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/{documentId}/attachment/{attachmentId}")
    public ResponseEntity removeAttachment(@PathVariable Long documentId, @PathVariable Long attachmentId) {
        documentService.removeAttachment(documentId, attachmentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/{documentId}/attachment/{attachmentId}")
    public ResponseEntity updateAttachmentInfo(@PathVariable Long documentId, @PathVariable Long attachmentId, @RequestBody Attachment attachment) {
        documentService.updateAttachment(documentId, attachmentId, attachment);
        return new ResponseEntity<>(HttpStatus.OK);
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

    /**
     * Fetches the navigation path to the document.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/documents/{documentId}/breadcrumbs
     * </pre>
     *
     * @param documentId the document id of the document that the breadcrumbs shall be retrieved for.
     * @return <b>200 OK</b>an array of parent documents (representation of a breadcrumb path), <b>404 NOT FOUND</b>
     */
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
     *     <b>Path:</b> POST /api/documents/{documentId}/subscribe
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

    /**
     * This resource allows it to retrieve all the notifications that a user got since his last visit.
     * <p>
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/documents/notifications
     * </pre>
     *
     * @return <b>200 OK</b> the notification that belong to this user.
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        List<Notification> notificationList = documentService.getNotifications();
        return new ResponseEntity<>(notificationList, HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{documentId}/share")
    public ResponseEntity<List<Notification>> shareDocumentWithUser(@PathVariable Long documentId, @RequestParam String userIds, @RequestParam String permissions) {
        documentService.addAccess(documentId, userIds, permissions);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/users/permission")
    public ResponseEntity<List<Access>> getUsersByDocumentPermission(@PathVariable Long documentId, @RequestParam String permissions) {
        List<Access> userList = documentService.getUsersByPermissions(documentId, permissions);
        return new ResponseEntity<>(userList, HttpStatus.OK);
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentId}/currentuser/permission")
    public ResponseEntity<Access> getCurrentUsersPermissions(@PathVariable Long documentId, @RequestParam String permissions) {
        Access access = documentService.getCurrentUserPermissions(documentId, permissions);
        return new ResponseEntity<>(access, HttpStatus.OK);
    }
}