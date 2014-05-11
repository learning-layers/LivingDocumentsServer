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

import de.hska.livingdocuments.core.dto.NodeDto;
import de.hska.livingdocuments.core.service.JcrService;
import de.hska.livingdocuments.core.util.Core;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Secured(Core.ROLE_USER)
    @RequestMapping(method = RequestMethod.GET, value = "/{nodeId}")
    public ResponseEntity<NodeDto> getNode(@PathVariable String nodeId, HttpServletResponse response) {
        Session session = null;
        try {
            session = jcrService.adminLogin();
            Node sandboxNode = session.getNode("/" + nodeId);

            Property descriptionProperty = sandboxNode.getProperty("description");

            NodeDto nodeDto = new NodeDto();
            nodeDto.setDescription(descriptionProperty.getString());

            return new ResponseEntity<>(nodeDto, HttpStatus.OK);
        } catch (RepositoryException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/download/{nodeId}")
    public void downloadNode(@PathVariable String nodeId, HttpServletResponse response) {
        Session session = null;
        try {
            session = jcrService.adminLogin();
            Node sandboxNode = session.getNode("/" + nodeId);
            InputStream inputStream = JcrUtils.readFile(sandboxNode.getNode("fileNode/" + JcrConstants.JCR_CONTENT));

            response.setContentType("application/pdf");

            try {
                OutputStream outputStream = response.getOutputStream();
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } finally {
            if (session != null) {
                session.logout();
            }
        }
    }
}
