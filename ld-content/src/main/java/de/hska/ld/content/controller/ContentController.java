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

import de.hska.ld.content.controller.resolver.JcrSession;
import de.hska.ld.content.dto.CommentNodeDto;
import de.hska.ld.content.dto.NodeDto;
import de.hska.ld.content.dto.TagDto;
import de.hska.ld.content.persistence.domain.Subscription;
import de.hska.ld.content.service.JcrService;
import de.hska.ld.content.service.SubscriptionService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.dto.TextDto;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.util.Core;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p><b>RESOURCE</b> {@code /api/content}
 */
@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private JcrService jcrService;

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * This resource allows it to create a document node.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/content/document/{documentNodeId}
     * </pre>
     * @param documentDto Contains title and optional description of the new document. Example:
     *                    {title: 'New Document', description: '&lt;optional&gt;'}
     * @return  <b>200 OK</b> with the generated node contents<br>
     *          <b>400 Bad Request</b> if no title exists
     *          <b>409 Conflict</b> if a node with the given id already exists<br>
     *          <b>500 Internal Server Error</b> if there occurred any other server side issue
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/document")
    public ResponseEntity<NodeDto> createDocumentNode(@JcrSession Session session, @RequestBody NodeDto documentDto) {
        try {
            if (documentDto.getTitle() == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Node documentNode = jcrService.createDocumentNode(session, documentDto.getTitle(), documentDto.getDescription());
            return new ResponseEntity<>(new NodeDto(documentNode), HttpStatus.OK);
        } catch (ItemExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes a document.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> DELETE /api/content/document/{documentNodeId}
     * </pre>
     *
     * @param documentNodeId the node id of the node one wants to delete
     * @return  <b>200 OK</b> if the removal of the document node has been successfully executed<br>
     *          <b>404 NOT FOUND</b> if a document node with the given id isn't present in this application<br>
     *          <b>500 Internal Server Error</b> if there occured any other server side issue
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/document/{documentNodeId}")
    public ResponseEntity removeDocumentNode(@PathVariable String documentNodeId, @JcrSession Session session) {
        try {
            jcrService.removeDocumentNode(session, documentNodeId);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * This resource allows uploading files.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/content/document/upload
     * </pre>
     *
     * @param file the Multipart file that has been uploaded
     * @param documentNodeId the documentNodeId to which the file shall be attached
     * @param cmd the command which decides where this file shall be attached within the document.<br>
     *            - "main" for attaching the file as a main content of a document<br>
     *               (attaches the file to the front of a document<br>     *
     *            - "attachment" for attaching the file to the back of a document as attachment
     * @return  <b>200 OK</b> if the upload has been successfully performed<br>
     *          <b>400 BAD REQUEST</b> if empty file parameter<br>
     *          <b>500 Internal Server Error</b> if there occurred any other server side issue
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/document/upload")
    public ResponseEntity uploadFile(@RequestParam MultipartFile file, @RequestParam String documentNodeId,
                                     @RequestParam String cmd, @JcrSession Session session) {
        String name = file.getOriginalFilename();
        if (!file.isEmpty()) {
            try {
                Node documentNode = jcrService.getNode(session, documentNodeId);
                jcrService.addFileNode(session, documentNode, file.getInputStream(), name, cmd);
                return new ResponseEntity(HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This resource allows downloading a file attachment.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path - option 1 (downloading an attachment):</b> GET /api/content/document/{documentNodeId}/download?attachment={attachmentNodeId}
     *     <b>Path - option 2 (download a main content):</b> GET /api/content/document/{documentNodeId}/download
     * </pre>
     *
     * @param documentNodeId the node id of the document that contains the needed attachment node
     * @param attachmentNodeId the node id of the attachment that is needed
     * @param response <b>FILE DOWNLOAD</b> if the attachment could be found, and the download is starting
     *                 <b>400 BAD REQUEST</b>
     *                 <b>403 FORBIDDEN</b> if the access to this attachment has been denied
     *                 <b>404</b> if no node has been found for a given document or attachment node id
     *                 <b>500</b> if there occured any other server side issue
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/document/{documentNodeId}/download")
    public void downloadFile(@PathVariable String documentNodeId, @RequestParam(required = false) String attachmentNodeId,
                         @JcrSession Session session, HttpServletResponse response) {
        try {
            Node fileNode;
            try {
                Node documentNode = jcrService.getNode(session, documentNodeId);
                if (!documentNode.getPrimaryNodeType().getName().equals(Content.LD_DOCUMENT)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                if (attachmentNodeId != null) {
                    Node attachmentsNode = documentNode.getNode(Content.LD_ATTACHMENTS_NODE);
                    fileNode = attachmentsNode.getNode(attachmentNodeId);
                } else {
                    fileNode = documentNode.getNode(Content.LD_MAIN_FILE_NODE);
                }
            } catch (PathNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            } catch (RepositoryException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            InputStream inputStream = JcrUtils.readFile(fileNode.getNode(JcrConstants.JCR_CONTENT));
            response.setContentType("application/pdf");
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds a comment node to either a parent document or a parent comment.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path</b> POST /api/content/{nodeId}/comment
     * </pre>
     *
     * @param nodeId the node id of the parent to which the new comment should be added
     * @param textDto the text content of the comment that shall be added
     * @return  <b>200 OK</b> and the node data if everything went fine<br>
     *          <b>404 NOT FOUND</b> if the parent node could not be found in the system
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/comment")
    public ResponseEntity<NodeDto> addCommentNode(@PathVariable String nodeId, @RequestBody TextDto textDto,
                                                  @JcrSession Session session) {
        try {
            Node documentOrCommentNode = jcrService.getNode(session, nodeId);
            Node commentNode = jcrService.addComment(session, documentOrCommentNode, textDto.getText());
            return new ResponseEntity<>(new NodeDto(commentNode), HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Updates an existing comment.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> PUT /api/content/comment
     * </pre>
     *
     * @param commentNodeDto the node content that contains the changes to this comment
     * @return  <b>200 OK</b> if the changes have been successfully applied<br>
     *          <b>404 NOT FOUND</b> if a comment with the given comment node id inside the node dto could not be found
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/comment")
    public ResponseEntity<NodeDto> updateCommentNode(@RequestBody NodeDto commentNodeDto, @JcrSession Session session) {
        try {
            Node rootNode = session.getRootNode();
            Node documentsNode = rootNode.getNode(Content.LD_DOCUMENTS);

            Node commentNode = documentsNode.getNode(commentNodeDto.getNodeId());
            jcrService.updateComment(session, commentNode, commentNodeDto.getDescription());
            return new ResponseEntity<>(new NodeDto(commentNode), HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Adds a tag to any node.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/content/{nodeId}/tag
     * </pre>
     *
     * @param nodeId the node id of a node that shall be tagged
     * @param tagDto that tag contents
     * @return  <b>200 OK</b> if the node has been successfully tagged with the given tag<br>
     *          <b>404 NOT FOUND</b> if there is no node with the given nodeId within the system<br>
     *          <b>409 CONFLICT</b> if the given tag has already been added to this node
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/tag")
    public ResponseEntity<TextDto> addTagNode(@PathVariable String nodeId, @RequestBody TagDto tagDto,
                                          @JcrSession Session session) {
        try {
            Node nodeToBeTagged = jcrService.getNode(session, nodeId);
            Node tagNode = jcrService.addTag(session, nodeToBeTagged, tagDto.getTagName(), tagDto.getDescription());

            TextDto nodeDto = new TextDto();
            nodeDto.setText(tagNode.getIdentifier());
            return new ResponseEntity<>(nodeDto, HttpStatus.OK);
        } catch (ItemExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Removes a tag from a node.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> DELETE /api/content/tag/remove?tagName=&lt;tagName&gt;
     * </pre>
     *
     * @param taggedNodeId the node id of the node that contains the tag
     * @param tagId the tag ID of the tag that shall be removed from the node
     * @return  <b>200 OK</b> if the tag has been removed from the node<br>
     *          <b>404 NOT FOUND</b> if there is no node with the given taggedNodeId present in the system<br>
     *          <b>409 CONFLICT</b> if there has been no tag with the given tagName present on the node
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.DELETE, value = "/tag/remove")
    public ResponseEntity removeTag(@RequestParam String taggedNodeId, @RequestParam String tagId,
                                    @JcrSession Session session) {
        try {
            Node node = jcrService.getNode(session, taggedNodeId);
            jcrService.removeTag(session, node, tagId);
        } catch (ItemNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (ConstraintViolationException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * This resource allows it to add a subscription for tracking changes to a specific node.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> POST /api/content/{nodeId}/subscribe
     * </pre>
     *
     * @param nodeId the node id of the node that shall be tracked
     * @param type the subscription type (e.g. DOCUMENT, ATTACHMENT, COMMENT, DISCUSSION, USER) depending on which
     *             part of a node the user wants to receive notifications for.
     * @return  <b>201 CREATED</b> if a subscription has been successfully applied to the node<br>
     *          <b>404 NOT FOUND</b> if the node could not be found within the system
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/subscribe")
    public ResponseEntity subscribe(@PathVariable String nodeId, Subscription.Type type, @JcrSession Session session,
                                    @AuthenticationPrincipal User user) {
        try {
            Node node = session.getNode("/" + nodeId);
            // TODO check access permission
        } catch (Exception e) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        Subscription subscription = new Subscription(nodeId, type, user);
        try {
            subscriptionService.save(subscription);
        } catch (Exception e) {
            // already exists
        }

        return new ResponseEntity(HttpStatus.CREATED);
    }

    /**
     * Fetch the meta data for a specific node.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/content/{nodeId}/meta
     * </pre>
     *
     * @param nodeId the node id the meta data shall be fetched for
     * @return  <b>200 OK</b> and the meta data<br>
     *          <b>404 NOT FOUND</b> if a node with the given nodeId isn't present within the system
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{nodeId}/meta")
    public ResponseEntity<NodeDto> getNodeMetaData(@PathVariable String nodeId, @JcrSession Session session) {
        try {
            Node node = jcrService.getNode(session, nodeId);
            NodeDto nodeMetaDto = new NodeDto(node);
            return new ResponseEntity<>(nodeMetaDto, HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Fetches the commment Nodes for a specifc node.
     *
     * <pre>
     *     <b>Required roles:</b> ROLE_USER
     *     <b>Path:</b> GET /api/content/{nodeId}/comments
     * </pre>
     *
     * @param nodeId the node id of the node the comments shall be fetched for
     * @return <b>200 OK</b> and a list of comments
     *         <b>404 NOT FOUND</b> if there is no node present within the system that has the specified nodeId
     */
    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{nodeId}/comments")
    public ResponseEntity<List<CommentNodeDto>> getCommentNodes(@PathVariable String nodeId,
                                                                @JcrSession Session session) {
        try {
            Node node = jcrService.getNode(session, nodeId);
            List<Node> commentList = jcrService.getComments(node);
            List<CommentNodeDto> dtoList = commentList.stream().map(CommentNodeDto::new).collect(Collectors.toList());
            return new ResponseEntity<>(dtoList, HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}