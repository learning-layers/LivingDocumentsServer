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
import de.hska.ld.content.util.Content;
import de.hska.ld.core.AbstractIntegrationTestOld;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class ContentControllerIntegrationTest extends AbstractIntegrationTestOld {

    private static final String RESOURCE_CONTENT = Content.RESOURCE_DOCUMENT;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    @Autowired
    UserService userService;

    Document document;

    @Before
    public void setUp() throws Exception {
        //super.setUp();
        User user = userService.findByUsername("user");
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
    }

//    @Test
//    public void thatCreateDocumentNodeUsesHttpOkOnPersist() throws RepositoryException {
//        ResponseEntity<NodeDto> response = exchange(RESOURCE_CONTENT + "/document", HttpMethod.POST,
//                createUserHeader(document), NodeDto.class);
//        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }

//    @Test
//    public void thatGetNodeMetaDataUsesHttpOkOnEntityLookupSuccess() throws RepositoryException {
//        Node node = jcrService.createDocumentNode(session, TITLE, DESCRIPTION);
//
//        ResponseEntity<NodeDto> response = exchange(RESOURCE_DOCUMENT + "/" + node.getName() + "/meta", HttpMethod.GET,
//                createUserHeader(), NodeDto.class);
//        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    @SuppressWarnings("unchecked")
//    public void thatGetCommentNodesUsesHttpOkOnEntityLookupSuccess() throws RepositoryException {
//        String testComment = "This is a test node";
//        Node node = jcrService.createDocumentNode(session, TITLE, DESCRIPTION);
//        jcrService.addComment(session, node, testComment);
//
//        ResponseEntity<List> response = exchange(RESOURCE_DOCUMENT + "/" + node.getName() + "/comments",
//                HttpMethod.GET, createUserHeader(), List.class);
//
//        Assert.assertTrue(response.getBody().size() == 1);
//        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    public void thatAddCommentNodeUsesHttpOkOnPersist() throws RepositoryException {
//        Node node = jcrService.createDocumentNode(session, TITLE, DESCRIPTION);
//
//        ResponseEntity<NodeDto> response = exchange(RESOURCE_DOCUMENT + "/" + node.getName() + "/comment",
//                HttpMethod.POST, createUserHeader(new TextDto("This is a test comment.")), NodeDto.class);
//        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    public void thatAddTagNodeUsesHttpOkOnPersist() throws RepositoryException {
//        Node node = jcrService.createDocumentNode(session, TITLE, DESCRIPTION);
//
//        ResponseEntity<TextDto> response = exchange(RESOURCE_DOCUMENT + "/" + node.getName() + "/tag",
//                HttpMethod.POST, createUserHeader(new TagDto("TagName", "The description")), TextDto.class);
//        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    public void thatSearchNodeUsesHttpOkAndRendersCorrectly() throws RepositoryException {
//        String term = "Testdocument";
//
//        Node node = jcrService.createDocumentNode(session, term, term);
//        ResponseEntity<List> response = exchange(RESOURCE_DOCUMENT + "/search?query=" + term,
//                HttpMethod.GET, createUserHeader(), List.class);
//
//        Assert.assertTrue(!response.getBody().isEmpty());
//        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    public void thatGetDocumentNodeListUsesHttpOk() throws RepositoryException {
//        jcrService.createDocumentNode(session, TITLE + 1, DESCRIPTION);
//        jcrService.createDocumentNode(session, TITLE + 2, DESCRIPTION);
//
//        ResponseEntity<List> response = exchange(RESOURCE_DOCUMENT + "/list", HttpMethod.GET,
//                createUserHeader(), List.class);
//
//        Assert.assertTrue(!response.getBody().isEmpty());
//        Assert.assertTrue(response.getBody().size() >= 2);
//        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
//    }
//
//    @Test
//    public void thatAddTagNodeUsesHttpConflictOnNodeAlreadyExists() throws RepositoryException {
//        String tagName = "TagName";
//        String description = "The description";
//        Node node = jcrService.createDocumentNode(session, TITLE, DESCRIPTION);
//        jcrService.addTag(session, node, tagName, description);
//
//        try {
//            exchange(RESOURCE_DOCUMENT + "/" + node.getName() + "/tag", HttpMethod.POST,
//                    createUserHeader(new TagDto(tagName, description)), List.class);
//        } catch (HttpStatusCodeException e) {
//            expectedClientException = e;
//        }
//        Assert.assertNotNull(expectedClientException);
//        Assert.assertEquals(HttpStatus.CONFLICT, expectedClientException.getStatusCode());
//    }
}
