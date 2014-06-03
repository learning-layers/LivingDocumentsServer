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

package de.hska.ld.core.controller;

import de.hska.ld.core.controller.resolver.JcrSession;
import de.hska.ld.core.dto.CommentNodeDto;
import de.hska.ld.core.dto.NodeDto;
import de.hska.ld.core.dto.TextDto;
import de.hska.ld.core.persistence.domain.Subscription;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.JcrService;
import de.hska.ld.core.service.SubscriptionService;
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
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p><b>RESOURCE</b> {@code /api/documents}
 */
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private JcrService jcrService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<NodeDto> createDocumentNode(@RequestBody @Valid NodeDto nodeDto, @JcrSession Session session) {
        try {
            Node documentNode = jcrService.createDocumentNode(session, nodeDto.getNodeId());
            return new ResponseEntity<>(new NodeDto(documentNode), HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity updateNode(@RequestBody @NotNull Map<String, String> updateMap, String nodeId, NodeDto nodeDto) {

        // TODO
        for (String key : updateMap.keySet()) {
            if (key.equals("updateMainFileName")) {
                String fileName = updateMap.get(key);

            }
        }

        return null;
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/comment")
    public ResponseEntity<NodeDto> addCommentNode(@PathVariable String nodeId, @RequestBody TextDto textDto,
                                                  @JcrSession Session session) {
        try {
            Node documentOrCommentNode = session.getNode("/" + nodeId);
            Node commentNode = jcrService.addComment(session, documentOrCommentNode, textDto.getText());
            return new ResponseEntity<>(new NodeDto(commentNode), HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.PUT, value = "/comment")
    public ResponseEntity<NodeDto> updateCommentNode(@RequestBody NodeDto commentNodeDto, @JcrSession Session session) {
        try {
            Node rootNode = session.getRootNode();
            Node documentsNode = rootNode.getNode(Core.LD_DOCUMENTS);

            Node commentNode = documentsNode.getNode(commentNodeDto.getNodeId());
            jcrService.updateComment(session, commentNode, commentNodeDto.getDescription());
            return new ResponseEntity<>(new NodeDto(commentNode), HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/tag")
    public ResponseEntity<NodeDto> addTag(@PathVariable String nodeId, @RequestBody NodeDto nodeDtoReq,
                                          @JcrSession Session session) {
        try {
            Node rootNode = session.getRootNode();
            Node documentsNode = rootNode.getNode(Core.LD_DOCUMENTS);
            Node node = documentsNode.getNode(nodeId);

            // call add tag in service method
            // !nodeDtoReq.getNodeId() contains the tagname
            Node tagNode = jcrService.addTag(session, documentsNode, node, nodeDtoReq.getNodeId(), nodeDtoReq.getDescription());

            // create response dto
            NodeDto nodeDtoRes = new NodeDto();
            nodeDtoRes.setNodeId(tagNode.getIdentifier());
            return new ResponseEntity<>(nodeDtoRes, HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

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

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity uploadFile(@RequestParam(value = "file", required = true) MultipartFile file,
                                     @JcrSession Session session,
                                     @RequestParam(required = true) String nodeId,
                                     @RequestParam(required = true) String cmd) {
        String name = file.getOriginalFilename();
        if (!file.isEmpty()) {
            try {
                Node documentNode = jcrService.getDocumentNode(session, nodeId);
                jcrService.addFileNode(session, documentNode, file.getInputStream(), name, cmd);
                return new ResponseEntity(HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{nodeId}/meta")
    public ResponseEntity<NodeDto> getNodeMetaData(@PathVariable String nodeId, @JcrSession Session session) {
        try {
            Node rootNode = session.getRootNode();
            Node node = rootNode.getNode(Core.LD_DOCUMENTS + "/" + nodeId);
            NodeDto nodeMetaDto = new NodeDto(node);
            return new ResponseEntity<>(nodeMetaDto, HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{documentNodeId}/comments")
    public ResponseEntity<List<CommentNodeDto>> getCommentNodes(@PathVariable String documentNodeId,
                                                                @JcrSession Session session) {
        try {
            Node rootNode = session.getRootNode();
            Node node = rootNode.getNode(Core.LD_DOCUMENTS + "/" + documentNodeId + "/" + Core.LD_COMMENTS_NODE);
            NodeIterator commentNodeIt = node.getNodes();
            List<CommentNodeDto> commentList = new ArrayList<>();
            while (commentNodeIt.hasNext()) {
                Node commentNode = commentNodeIt.nextNode();
                commentList.add(new CommentNodeDto(commentNode));
            }
            return new ResponseEntity<>(commentList, HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/download/{documentNodeId}")
    public void download(@PathVariable String documentNodeId, @JcrSession Session session,
                         HttpServletResponse response) {
        try {
            Node documentNode;
            try {
                documentNode = session.getNode("/" + documentNodeId);
            } catch (PathNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            } catch (RepositoryException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            if (!documentNode.getPrimaryNodeType().getName().equals(Core.LD_DOCUMENT)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            InputStream inputStream = JcrUtils.readFile(documentNode
                    .getNode(Core.LD_FILE_NODE + "/" + JcrConstants.JCR_CONTENT));
            response.setContentType("application/pdf");
            OutputStream outputStream = response.getOutputStream();
            IOUtils.copy(inputStream, outputStream);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}