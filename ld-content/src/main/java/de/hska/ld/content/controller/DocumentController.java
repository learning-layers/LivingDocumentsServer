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

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.util.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p><b>Resource:</b> {@value Content#RESOURCE_DOCUMENT}
 */
@RestController
@RequestMapping(Content.RESOURCE_DOCUMENT)
public class DocumentController {

    @Autowired
    private DocumentService documentService;

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
        Page<Document> documentsPage = documentService.getDocumentPage(pageNumber, pageSize, sortDirection, sortProperty);
        if (documentsPage != null) {
            return new ResponseEntity<>(documentsPage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    /**
//     * This resource allows it to create a document node.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/document/{documentNodeId}
//     * </pre>
//     *
//     * @param documentDto Contains title and optional description of the new document. Example:
//     *                    {title: 'New Document', description: '&lt;optional&gt;'}
//     * @return <b>200 OK</b> with the generated node contents<br>
//     * <b>400 Bad Request</b> if no title exists<br>
//     * <b>409 Conflict</b> if a node with the given id already exists<br>
//     * <b>500 Internal Server Error</b> if there occurred any other server side issue
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.POST, value = "/document")
//    public ResponseEntity<NodeDto> createDocumentNode(@JcrSession Session session, @RequestBody NodeDto documentDto) {
//        if (documentDto.getTitle() == null) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//        try {
//            Node documentNode = jcrService.createDocumentNode(session, documentDto.getTitle(), documentDto.getDescription());
//            return new ResponseEntity<>(new NodeDto(documentNode), HttpStatus.OK);
//        } catch (ItemExistsException e) {
//            return new ResponseEntity<>(HttpStatus.CONFLICT);
//        } catch (RepositoryException e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
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
//
//
//    /**
//     * Deletes a document.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path:</b> DELETE {@value Content#RESOURCE_DOCUMENT}/document/{documentNodeId}
//     * </pre>
//     *
//     * @param documentNodeId the node id of the node one wants to delete
//     * @return <b>200 OK</b> if the removal of the document node has been successfully executed<br>
//     * <b>404 NOT FOUND</b> if a document node with the given id isn't present in this application<br>
//     * <b>500 Internal Server Error</b> if there occured any other server side issue
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.DELETE, value = "/document/{documentNodeId}")
//    public ResponseEntity removeDocumentNode(@PathVariable String documentNodeId, @JcrSession Session session) {
//        try {
//            jcrService.removeDocumentNode(session, documentNodeId);
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        } catch (RepositoryException e) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    /**
//     * This resource allows uploading files.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/document/upload
//     * </pre>
//     *
//     * @param file           the Multipart file that has been uploaded
//     * @param documentNodeId the documentNodeId to which the file shall be attached
//     * @param cmd            the command which decides where this file shall be attached within the document.<br>
//     *                       <ul>
//     *                       <li><b>main</b> for attaching the file as a main content of a document<br>
//     *                       (attaches the file to the front of a document)<br></li>
//     *                       <li><b>attachment</b> for attaching the file to the back of a document as attachment</li>
//     *                       </ul>
//     * @return <b>200 OK</b> if the upload has been successfully performed<br>
//     * <b>400 BAD REQUEST</b> if empty file parameter<br>
//     * <b>500 Internal Server Error</b> if there occurred any other server side issue
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.POST, value = "/document/upload")
//    public ResponseEntity uploadFile(@RequestParam MultipartFile file, @RequestParam String documentNodeId,
//                                     @RequestParam String cmd, @JcrSession Session session) {
//        String name = file.getOriginalFilename();
//        if (!file.isEmpty()) {
//            try {
//                Node documentNode = jcrService.getNode(session, documentNodeId);
//                jcrService.addFileNode(session, documentNode, file.getInputStream(), name, cmd);
//                return new ResponseEntity(HttpStatus.OK);
//            } catch (Exception e) {
//                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        } else {
//            return new ResponseEntity(HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    /**
//     * This resource allows downloading a file attachment.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path - option 1 (downloading an attachment):</b> GET {@value Content#RESOURCE_DOCUMENT}/document/{documentNodeId}/download?attachment={attachmentNodeId}
//     *     <b>Path - option 2 (download a main content):</b> GET {@value Content#RESOURCE_DOCUMENT}/document/{documentNodeId}/download
//     * </pre>
//     *
//     * @param documentNodeId   the node id of the document that contains the needed attachment node
//     * @param attachmentNodeId the node id of the attachment that is needed
//     * @param response         <b>FILE DOWNLOAD INITIATED</b> if the attachment could be found, and the download is starting<br>
//     *                         <b>400 BAD REQUEST</b><br>
//     *                         <b>403 FORBIDDEN</b> if the access to this attachment has been denied<br>
//     *                         <b>404 NOT FOUND</b> if no node has been found for a given document or attachment node id<br>
//     *                         <b>500 Internal Server Error</b> if there occurred any other server side issue
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.GET, value = "/document/{documentNodeId}/download")
//    public void downloadFile(@PathVariable String documentNodeId, @RequestParam(required = false) String attachmentNodeId,
//                             @JcrSession Session session, HttpServletResponse response) {
//        try {
//            Node fileNode;
//            try {
//                Node documentNode = jcrService.getNode(session, documentNodeId);
//                if (!documentNode.getPrimaryNodeType().getName().equals(Content.LD_DOCUMENT)) {
//                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
//                    return;
//                }
//                if (attachmentNodeId != null) {
//                    Node attachmentsNode = documentNode.getNode(Content.LD_ATTACHMENTS_NODE);
//                    fileNode = attachmentsNode.getNode(attachmentNodeId);
//                } else {
//                    fileNode = documentNode.getNode(Content.LD_MAIN_FILE_NODE);
//                }
//            } catch (PathNotFoundException e) {
//                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                return;
//            } catch (RepositoryException e) {
//                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                return;
//            }
//
//            InputStream inputStream = JcrUtils.readFile(fileNode.getNode(JcrConstants.JCR_CONTENT));
//            response.setContentType("application/pdf");
//            OutputStream outputStream = response.getOutputStream();
//            IOUtils.copy(inputStream, outputStream);
//        } catch (Exception e) {
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * Adds a comment node to either a parent document or a parent comment.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path</b> POST {@value Content#RESOURCE_DOCUMENT}/{nodeId}/comment
//     * </pre>
//     *
//     * @param nodeId  the node id of the parent to which the new comment should be added
//     * @param textDto the text content of the comment that shall be added
//     * @return <b>200 OK</b> and the node data if everything went fine<br>
//     * <b>404 NOT FOUND</b> if the parent node could not be found in the system
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/comment")
//    public ResponseEntity<NodeDto> addCommentNode(@PathVariable String nodeId, @RequestBody TextDto textDto,
//                                                  @JcrSession Session session) {
//        try {
//            Node documentOrCommentNode = jcrService.getNode(session, nodeId);
//            Node commentNode = jcrService.addComment(session, documentOrCommentNode, textDto.getText());
//            return new ResponseEntity<>(new NodeDto(commentNode), HttpStatus.OK);
//        } catch (RepositoryException e) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    /**
//     * Updates an existing comment.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path:</b> PUT {@value Content#RESOURCE_DOCUMENT}/comment
//     * </pre>
//     *
//     * @param commentNodeDto the node content that contains the changes to this comment. Example:<br>
//     *                       <tt>{id: 'nodeId', description: 'The comment description'}</tt>
//     * @return <b>200 OK</b> if the changes have been successfully applied<br>
//     * <b>404 NOT FOUND</b> if a comment with the given comment node id inside the node dto could not be found
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.PUT, value = "/comment")
//    public ResponseEntity<NodeDto> updateCommentNode(@RequestBody NodeDto commentNodeDto, @JcrSession Session session) {
//        try {
//            Node rootNode = session.getRootNode();
//            Node documentsNode = rootNode.getNode(Content.LD_DOCUMENTS);
//
//            Node commentNode = documentsNode.getNode(commentNodeDto.getNodeId());
//            jcrService.updateComment(session, commentNode, commentNodeDto.getDescription());
//            return new ResponseEntity<>(new NodeDto(commentNode), HttpStatus.OK);
//        } catch (RepositoryException e) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    /**
//     * Adds a tag to any node.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/{nodeId}/tag
//     * </pre>
//     *
//     * @param nodeId the node id of a node that shall be tagged
//     * @param tagDto the tag contents. Example:<br>
//     *               {tagName: '', description: ''}
//     * @return <b>200 OK</b> if the node has been successfully tagged with the given tag<br>
//     * <b>404 NOT FOUND</b> if there is no node with the given nodeId within the system<br>
//     * <b>409 CONFLICT</b> if the given tag has already been added to this node
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/tag")
//    public ResponseEntity<TextDto> addTagNode(@PathVariable String nodeId, @RequestBody TagDto tagDto,
//                                              @JcrSession Session session) {
//        try {
//            Node nodeToBeTagged = jcrService.getNode(session, nodeId);
//            Node tagNode = jcrService.addTag(session, nodeToBeTagged, tagDto.getTagName(), tagDto.getDescription());
//
//            TextDto nodeDto = new TextDto();
//            nodeDto.setText(tagNode.getName());
//            return new ResponseEntity<>(nodeDto, HttpStatus.OK);
//        } catch (ItemExistsException e) {
//            return new ResponseEntity<>(HttpStatus.CONFLICT);
//        } catch (RepositoryException e) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    /**
//     * Removes a tag from a node.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path:</b> DELETE {@value Content#RESOURCE_DOCUMENT}/tag/remove?tagName=&lt;tagName&gt;
//     * </pre>
//     *
//     * @param taggedNodeId the node id of the node that contains the tag
//     * @param tagId        the tag ID of the tag that shall be removed from the node
//     * @return <b>200 OK</b> if the tag has been removed from the node<br>
//     * <b>404 NOT FOUND</b> if there is no node with the given taggedNodeId present in the system<br>
//     * <b>409 CONFLICT</b> if there has been no tag with the given tagName present on the node
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.DELETE, value = "/tag/remove")
//    public ResponseEntity removeTag(@RequestParam String taggedNodeId, @RequestParam String tagId,
//                                    @JcrSession Session session) {
//        try {
//            Node node = jcrService.getNode(session, taggedNodeId);
//            jcrService.removeTag(session, node, tagId);
//        } catch (ItemNotFoundException e) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        } catch (ConstraintViolationException e) {
//            return new ResponseEntity<>(HttpStatus.CONFLICT);
//        } catch (RepositoryException e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    /**
//     * This resource allows it to add a subscription for tracking changes to a specific node.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path:</b> POST {@value Content#RESOURCE_DOCUMENT}/{nodeId}/subscribe
//     * </pre>
//     *
//     * @param nodeId the node id of the node that shall be tracked
//     * @param type   the subscription type (e.g. DOCUMENT, ATTACHMENT, COMMENT, DISCUSSION, USER) depending on which
//     *               part of a node the user wants to receive notifications for.
//     * @return <b>201 CREATED</b> if a subscription has been successfully applied to the node<br>
//     * <b>404 NOT FOUND</b> if the node could not be found within the system
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/subscribe")
//    public ResponseEntity subscribe(@PathVariable String nodeId, Subscription.Type type, @JcrSession Session session,
//                                    @AuthenticationPrincipal User user) {
//        try {
//            Node node = session.getNode("/" + nodeId);
//            // TODO check access permission
//        } catch (Exception e) {
//            return new ResponseEntity(HttpStatus.NOT_FOUND);
//        }
//
//        Subscription subscription = new Subscription(nodeId, type, user);
//        try {
//            subscriptionService.save(subscription);
//        } catch (Exception e) {
//            // already exists
//        }
//
//        return new ResponseEntity(HttpStatus.CREATED);
//    }
//
//    /**
//     * Fetch the meta data for a specific node.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path:</b> GET /api/content/{nodeId}/meta
//     * </pre>
//     *
//     * @param nodeId the node id the meta data shall be fetched for
//     * @return <b>200 OK</b> and the meta data<br>
//     * <b>404 NOT FOUND</b> if a node with the given nodeId isn't present within the system
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.GET, value = "/{nodeId}/meta")
//    public ResponseEntity<NodeDto> getNodeMetaData(@PathVariable String nodeId, @JcrSession Session session) {
//        try {
//            Node node = jcrService.getNode(session, nodeId);
//            NodeDto nodeMetaDto = new NodeDto(node);
//            return new ResponseEntity<>(nodeMetaDto, HttpStatus.OK);
//        } catch (RepositoryException e) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    /**
//     * Fetches the commment Nodes for a specifc node.
//     * <p>
//     * <pre>
//     *     <b>Required roles:</b> ROLE_USER
//     *     <b>Path:</b> GET /api/content/{nodeId}/comments
//     * </pre>
//     *
//     * @param nodeId the node id of the node the comments shall be fetched for
//     * @return <b>200 OK</b> and a list of comments
//     * <b>404 NOT FOUND</b> if there is no node present within the system that has the specified nodeId
//     */
//    @Secured(Core.ROLE_USER)
//    @RequestMapping(method = RequestMethod.GET, value = "/{nodeId}/comments")
//    public ResponseEntity<List<CommentNodeDto>> getCommentNodes(@PathVariable String nodeId,
//                                                                @JcrSession Session session) {
//        try {
//            Node node = jcrService.getNode(session, nodeId);
//            List<Node> commentList = jcrService.getComments(node);
//            List<CommentNodeDto> dtoList = commentList.stream().map(CommentNodeDto::new).collect(Collectors.toList());
//            return new ResponseEntity<>(dtoList, HttpStatus.OK);
//        } catch (RepositoryException e) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
}