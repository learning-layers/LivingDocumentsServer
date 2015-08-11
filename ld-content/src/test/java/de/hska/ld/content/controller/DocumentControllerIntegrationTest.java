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
import de.hska.ld.core.service.UserService;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

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
    public void testGetDocumentsPageHttpOk() throws Exception {
        //Add document
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        //Get-request and assertion
        HttpResponse responseGet = UserSession.user().get(RESOURCE_DOCUMENT);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseGet));
    }

    //TODO: 405 is returned. Shouldn't it be 403?
    @Test
    public void testGetDocumentsPageHttpForbidden() throws Exception {
        //Add document
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        //Get-request and assertion
        HttpResponse responseGet = UserSession.notAuthenticated().get(RESOURCE_DOCUMENT);
        Assert.assertEquals(HttpStatus.FORBIDDEN, ResponseHelper.getStatusCode(responseGet));
    }

    @Test
    public void testGetDiscussionsPageHttpOk() throws Exception {
        //Add document
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Document respondedDocument = ResponseHelper.getBody(response, Document.class);
        //Add discussion
        String uri = RESOURCE_DOCUMENT + "/" + respondedDocument.getId() + "/discussion";
        HttpResponse response2 = UserSession.user().post(uri, document);
        //Get-request and assertion
        uri = uri + "s";
        HttpResponse responseGet = UserSession.user().get(uri);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseGet));
    }

    @Test
    public void testCreateDocumentUsesHttpOkOnPersist() throws Exception {
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
        Document responseDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNotNull(responseDocument);
        Assert.assertNotNull(responseDocument.getId());
    }

    @Test
    public void testShouldFailCreateDocumentNotAuthenticated() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
        HttpResponse response = UserSession.notAuthenticated().post(RESOURCE_DOCUMENT, document);
        Assert.assertTrue(UserSession.isNotAuthenticatedResponse(response));
        Document responseDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNull(responseDocument);
    }

    @Test
    public void testGETDocumentPageHttpOk() throws Exception {
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
        Document responseDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNotNull(responseDocument);
        Assert.assertNotNull(responseDocument.getId());

        HttpResponse responseGetPage = UserSession.user().get(RESOURCE_DOCUMENT + "/" + responseDocument.getId());
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseGetPage));
    }

    @Test
    public void testRemoveDocumentHttpOk() throws Exception {
        // Add document
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
        Document responseCreateDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNotNull(responseCreateDocument);
        Assert.assertNotNull(responseCreateDocument.getId());

        // Remove document
        String uriRemoveDocument = RESOURCE_DOCUMENT + "/" + responseCreateDocument.getId();
        HttpResponse responseDeleteDocument = UserSession.user().delete(uriRemoveDocument);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseDeleteDocument));

        // Try to access the document after it has been deleted
        HttpResponse responseDocumentNotPresent = UserSession.user().get(uriRemoveDocument);
        Assert.assertEquals(HttpStatus.NOT_FOUND, ResponseHelper.getStatusCode(responseDocumentNotPresent));
    }

    @Test
    public void testAddCommentHttpOk() throws Exception {
        // Add document
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
        Document responseCreateDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNotNull(responseCreateDocument);
        Assert.assertNotNull(responseCreateDocument.getId());

        // Add comment to the document
        String uriCommentDocument = RESOURCE_DOCUMENT + "/" + responseCreateDocument.getId() + RESOURCE_COMMENT;
        Comment comment = new Comment();
        comment.setText("Text");
        HttpResponse response2 = UserSession.user().post(uriCommentDocument, comment);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response2));
        Comment responseCreateComment = ResponseHelper.getBody(response2, Comment.class);
        Assert.assertNotNull(responseCreateComment);
        Assert.assertNotNull(responseCreateComment.getId());

        // get comments page
        HttpResponse responseGetCommentList = UserSession.user().get(uriCommentDocument);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseGetCommentList));
        List<Comment> commentList = ResponseHelper.getPageList(responseGetCommentList, Comment.class);
        Assert.assertTrue(commentList.size() > 0);
        Assert.assertTrue(commentList.contains(responseCreateComment));
    }

    @Test
    public void testAddAndRemoveTagHttpOk() throws Exception {
        // Add document
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
        Document respondedDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNotNull(respondedDocument);
        Assert.assertNotNull(respondedDocument.getId());

        // Create Tag
        HttpResponse responseCreateTag = UserSession.user().post(RESOURCE_TAG, tag);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(responseCreateTag));
        Tag respondedTag = ResponseHelper.getBody(responseCreateTag, Tag.class);
        Assert.assertNotNull(respondedTag);
        Assert.assertNotNull(respondedTag.getId());

        // Add Tag
        String URI = RESOURCE_DOCUMENT + "/" + respondedDocument.getId() + "/tag/" + respondedTag.getId();
        HttpResponse responseAddTag = UserSession.user().post(URI, respondedTag);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseAddTag));

        // Check if Tag is present in the taglist of the document
        String URIGetDocumentTags = RESOURCE_DOCUMENT + "/" + respondedDocument.getId() + "/tags";
        HttpResponse responseGetTags = UserSession.user().get(URIGetDocumentTags);
        List<Tag> respondedTagList = ResponseHelper.getPageList(responseGetTags, Tag.class);
        Assert.assertTrue(respondedTagList.size() > 0);
        Assert.assertTrue(respondedTagList.contains(respondedTag));

        // Remove tag
        HttpResponse responseRemoveTag = UserSession.user().delete(URI);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseRemoveTag));

        //Check if tag is present in the taglist of the document
        responseGetTags = UserSession.user().get(URIGetDocumentTags);
        respondedTagList = ResponseHelper.getPageList(responseGetTags, Tag.class);
        Assert.assertNull(respondedTagList);
    }

}
