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

import de.hska.ld.content.dto.CommentDto;
import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
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

public class CommentControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_DOCUMENT = Content.RESOURCE_DOCUMENT;
    private static final String RESOURCE_COMMENT = Content.RESOURCE_COMMENT;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    Document document;
    Document testDocument;
    Comment testComment;

    @Autowired
    UserService userService;

    @Autowired
    DocumentService documentService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(testUser);
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);

        //Add a document which is used for testing the CommentController
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        testDocument = ResponseHelper.getBody(response, Document.class);
        //Add a comment to the document
        String uriCommentDocument = RESOURCE_DOCUMENT + "/" + testDocument.getId() + "/comment";
        Comment comment = new Comment();
        comment.setText("Text");
        response = UserSession.user().post(uriCommentDocument, comment);
        testComment = ResponseHelper.getBody(response, Comment.class);
    }

    @Test
    public void testGetCommentsPageHttpOk() throws Exception {
        String uri = RESOURCE_COMMENT +"/" + testComment.getId() + "/comment";
        HttpResponse response = UserSession.user().get(uri);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testGetCommentsPageHttpNotFound() throws Exception {
        String invalidCommentPageUri = RESOURCE_COMMENT + "/-1" + "/comment";
        HttpResponse response = UserSession.user().get(invalidCommentPageUri);
        Assert.assertEquals(HttpStatus.NOT_FOUND, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testGetCommentsListHttpOk() throws Exception {
        String uri = RESOURCE_COMMENT +"/" + testComment.getId() + "/comment/list";
        HttpResponse response = UserSession.user().get(uri);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(response));
    }

    //TODO: Test fails and 200 is returned. Method in CommentServiceImpl doesn't throw exception yet
    @Test
    public void testGetCommentsListHttpNotFound() throws Exception {
        String invalidCommentPageUri = RESOURCE_COMMENT + "/-1" + "/comment/list";
        HttpResponse response = UserSession.user().get(invalidCommentPageUri);
        Assert.assertEquals(HttpStatus.NOT_FOUND, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testAgreeToCommentHttpOk() throws Exception {
        String uri = RESOURCE_COMMENT +"/" + testComment.getId() + "/agree";
        HttpResponse response = UserSession.user().put(uri, null);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testAddCommentToCommentHttpOk() throws Exception {
        String  uri = RESOURCE_COMMENT +"/" + testComment.getId() + "/comment";
        Comment subComment = new Comment();
        subComment.setText("Text");
        HttpResponse responseSubComment = UserSession.user().post(uri, subComment);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(responseSubComment));
        CommentDto commentDto = ResponseHelper.getBody(responseSubComment, CommentDto.class);
        Assert.assertNotNull(commentDto.getJsonParentId());
    }



}
