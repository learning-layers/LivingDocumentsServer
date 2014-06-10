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

import de.hska.ld.content.dto.NodeDto;
import de.hska.ld.content.dto.TagDto;
import de.hska.ld.content.service.JcrService;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.dto.TextDto;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.List;
import java.util.UUID;

public class ContentControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String CONTENT_RESOURCE = "/content";

    @Autowired
    JcrService jcrService;

    @Autowired
    UserService userService;

    Session session;

    @Before
    public void setUp() throws Exception {
        User user = userService.findByUsername("user");
        session = jcrService.login(user);
    }

    @Test
    public void thatCreateDocumentNodeUsesHttpOkOnPersist() throws RepositoryException {
        ResponseEntity<NodeDto> response = exchange(CONTENT_RESOURCE + "/document/" + UUID.randomUUID().toString(),
                HttpMethod.POST, createUserHeader(), NodeDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void thatCreateDocumentNodeUsesHttpConflictOnNodeAlreadyExists() throws RepositoryException {
        Node node = jcrService.createDocumentNode(session, UUID.randomUUID().toString());
        try {
            exchange(CONTENT_RESOURCE + "/document/" + node.getName(), HttpMethod.POST, createUserHeader(), NodeDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.CONFLICT, expectedClientException.getStatusCode());
    }

    @Test
    public void thatGetNodeMetaDataUsesHttpOkOnEntityLookupSuccess() throws RepositoryException {
        Node node = jcrService.createDocumentNode(session, UUID.randomUUID().toString());

        ResponseEntity<NodeDto> response = exchange(CONTENT_RESOURCE + "/" + node.getName() + "/meta", HttpMethod.GET,
                createUserHeader(), NodeDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void thatGetCommentNodesUsesHttpOkOnEntityLookupSuccess() throws RepositoryException {
        String testComment = "This is a test node";
        Node node = jcrService.createDocumentNode(session, UUID.randomUUID().toString());
        jcrService.addComment(session, node, testComment);

        ResponseEntity<List> response = exchange(CONTENT_RESOURCE + "/" + node.getName() + "/comments",
                HttpMethod.GET, createUserHeader(), List.class);

        Assert.assertTrue(response.getBody().size() == 1);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void thatAddCommentNodeUsesHttpOkOnPersist() throws RepositoryException {
        Node node = jcrService.createDocumentNode(session, UUID.randomUUID().toString());

        ResponseEntity<NodeDto> response = exchange(CONTENT_RESOURCE + "/" + node.getName() + "/comment",
                HttpMethod.POST, createUserHeader(new TextDto("This is a test comment.")), NodeDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void thatAddTagNodeUsesHttpOkOnPersist() throws RepositoryException {
        Node node = jcrService.createDocumentNode(session, UUID.randomUUID().toString());

        ResponseEntity<TextDto> response = exchange(CONTENT_RESOURCE + "/" + node.getName() + "/tag",
                HttpMethod.POST, createUserHeader(new TagDto("TagName", "The description")), TextDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void thatAddTagNodeUsesHttpConflictOnNodeAlreadyExists() throws RepositoryException {
        String tagName = "TagName";
        String description = "The description";
        Node node = jcrService.createDocumentNode(session, UUID.randomUUID().toString());
        jcrService.addTag(session, node, tagName, description);

        try {
            exchange(CONTENT_RESOURCE + "/" + node.getName() + "/tag", HttpMethod.POST,
                    createUserHeader(new TagDto(tagName, description)), List.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.CONFLICT, expectedClientException.getStatusCode());
    }
}
