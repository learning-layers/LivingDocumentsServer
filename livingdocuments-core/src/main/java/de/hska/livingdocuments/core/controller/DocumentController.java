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

package de.hska.livingdocuments.core.controller;

import de.hska.livingdocuments.core.controller.resolver.JcrSession;
import de.hska.livingdocuments.core.dto.NodeDto;
import de.hska.livingdocuments.core.dto.meta.NodeMetaDto;
import de.hska.livingdocuments.core.persistence.domain.Subscription;
import de.hska.livingdocuments.core.persistence.domain.User;
import de.hska.livingdocuments.core.service.JcrService;
import de.hska.livingdocuments.core.service.SubscriptionService;
import de.hska.livingdocuments.core.util.Core;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.InputStream;
import java.io.OutputStream;

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
    public ResponseEntity<NodeDto> saveDocumentNode(@RequestBody @Valid NodeDto nodeDto, @JcrSession Session session) {
        try {
            Node documentNode = jcrService.createDocumentNode(session, nodeDto.getNodeId());
            return new ResponseEntity<>(new NodeDto(documentNode), HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{nodeId}/meta")
    public ResponseEntity<NodeMetaDto> getNodeMetaData(@PathVariable String nodeId, @AuthenticationPrincipal User user,
                                                       @JcrSession Session session, HttpServletResponse response) {
        try {
            Node node = session.getNode("/" + nodeId);
            NodeMetaDto nodeMetaDto = new NodeMetaDto(node);

            return new ResponseEntity<>(nodeMetaDto, HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/download/{documentNodeId}")
    public void downloadNode(@PathVariable String documentNodeId, @AuthenticationPrincipal User user,
                             @JcrSession Session session, HttpServletResponse response) {
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

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.POST, value = "/{nodeId}/subscribe")
    public ResponseEntity subscribe(@PathVariable String nodeId, Subscription.Type type,
                                    @JcrSession Session session, @AuthenticationPrincipal User user) {
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
}