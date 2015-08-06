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

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.ResponseHelper;
import de.hska.ld.core.UserSession;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_DOCUMENT = Content.RESOURCE_DOCUMENT;
    private static final String RESOURCE_COMMENT = "/comment";
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";
    private static final String RESOURCE_TAG = Content.RESOURCE_TAG;

    @Autowired
    UserService userService;

    @Autowired
    DocumentService documentService;

    Document document;

    Tag tag;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
        tag = new Tag();
        tag.setName("tagName");
        tag.setDescription("tagDescription");
    }

    @Test
    public void testCreateDocumentUsesHttpOkOnPersist() {
        ResponseEntity<Document> response = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());
    }

    @Test
    public void testShouldFailCreateDocumentNotAuthenticated() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
        HttpResponse response = UserSession.notAuthenticated().post(RESOURCE_DOCUMENT, document);
        Assert.assertTrue(UserSession.isNotAuthenticatedResponse(response));
        Document reponseDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNull(reponseDocument);
    }

    @Test
    public void testGETDocumentPageHttpOk() {
        ResponseEntity<Document> response = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());

        Map varMap = new HashMap<>();
        varMap.put("page-number", 0);
        varMap.put("page-size", 10);
        varMap.put("sort-direction", "DESC");
        varMap.put("sort-property", "createdAt");
        User user = new User();
        user.setUsername("user");
        Map page = getPage(RESOURCE_DOCUMENT, user, varMap);
        Assert.assertNotNull(page);
        Assert.assertNotNull(page.containsKey("content"));
        Assert.assertTrue(((List) page.get("content")).size() > 0);
    }

    @Test
    public void testRemoveDocumentHttpOk() throws Exception {
        // Add document
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Assert.assertEquals(HttpStatus.CREATED, UserSession.getStatusCode(response));
        Document responseCreateDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNotNull(responseCreateDocument);
        Assert.assertNotNull(responseCreateDocument.getId());

        // Remove document
        String uriRemoveDocument = RESOURCE_DOCUMENT + "/" + responseCreateDocument.getId();
        HttpResponse responseDeleteDocument = UserSession.user().delete(uriRemoveDocument);
        Assert.assertEquals(HttpStatus.OK, UserSession.getStatusCode(responseDeleteDocument));

        // Try to access the document after it has been deleted
        HttpResponse responseDocumentNotPresent = UserSession.user().get(uriRemoveDocument);
        Assert.assertEquals(HttpStatus.NOT_FOUND, UserSession.getStatusCode(responseDocumentNotPresent));
    }

    @Test
    public void testAddCommentHttpOk() throws Exception {
        // Add document
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Assert.assertEquals(HttpStatus.CREATED, UserSession.getStatusCode(response));
        Document responseCreateDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNotNull(responseCreateDocument);
        Assert.assertNotNull(responseCreateDocument.getId());

        // Add comment to the document
        String uriCommentDocument = RESOURCE_DOCUMENT + "/" + responseCreateDocument.getId() + RESOURCE_COMMENT;
        Comment comment = new Comment();
        comment.setText("Text");
        HttpResponse response2 = UserSession.user().post(uriCommentDocument, comment);
        Assert.assertEquals(HttpStatus.CREATED, UserSession.getStatusCode(response2));
        Comment responseCreateComment = ResponseHelper.getBody(response2, Comment.class);
        Assert.assertNotNull(responseCreateComment);
        Assert.assertNotNull(responseCreateComment.getId());

        // get comments page
        HttpResponse responseGetCommentList = UserSession.user().get(uriCommentDocument);
        Assert.assertEquals(HttpStatus.OK, UserSession.getStatusCode(responseGetCommentList));
        List<Comment> commentList = ResponseHelper.getPageList(responseGetCommentList, Comment.class);
        Assert.assertTrue(commentList.size() > 0);
        Assert.assertTrue(commentList.contains(responseCreateComment));
    }

    @Test
    public void testAddAndRemoveTagHttpOk() {
        // Add document
        ResponseEntity<Document> responseCreateDocument = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, responseCreateDocument.getStatusCode());
        Assert.assertNotNull(responseCreateDocument.getBody().getId());

        // Create Tag
        ResponseEntity<Tag> responseCreateTag = post().resource(RESOURCE_TAG).asUser().body(tag).exec(Tag.class);
        Assert.assertEquals(HttpStatus.CREATED, responseCreateTag.getStatusCode());
        Assert.assertNotNull(responseCreateTag.getBody().getId());

        // Add Tag
        String URI = RESOURCE_DOCUMENT + "/" + responseCreateDocument.getBody().getId() + "/tag/" + responseCreateTag.getBody().getId();
        HttpRequestWrapper requestAddTag = post().resource(URI).asUser();
        ResponseEntity responseAddTag = requestAddTag.exec();
        Assert.assertEquals(HttpStatus.OK, responseAddTag.getStatusCode());

        // Check if Tag is present in the taglist of the document
        String URIGetDocumentTags = RESOURCE_DOCUMENT + "/" + responseCreateDocument.getBody().getId() + "/tags";
        Map varMap = new HashMap<>();
        varMap.put("page-number", 0);
        varMap.put("page-size", 10);
        varMap.put("sort-direction", "DESC");
        varMap.put("sort-property", "createdAt");
        Map page = getPage(URIGetDocumentTags, testUser, varMap);
        Assert.assertNotNull(page);
        Assert.assertNotNull(page.containsKey("content"));
        Assert.assertTrue(((List) page.get("content")).size() > 0);

        // Remove tag
        HttpRequestWrapper requestRemoveTag = delete().resource(URI).asUser();
        ResponseEntity responseRemoveTag = requestRemoveTag.exec();
        Assert.assertEquals(HttpStatus.OK, responseRemoveTag.getStatusCode());

        // Check if Tag is present in the taglist of the document
        boolean foundResult = true;
        try {
            Map page2 = getPage(URIGetDocumentTags, testUser, varMap);
            Assert.assertNotNull(page2);
            Assert.assertNotNull(page2.containsKey("content"));
            Assert.assertTrue(((List) page2.get("content")).size() == 0);
        } catch (HttpClientErrorException e) {
            Assert.assertEquals(e.getStatusCode().toString(), "404");
            foundResult = false;
        }
        if (foundResult) {
            Assert.fail();
        }
    }

}
