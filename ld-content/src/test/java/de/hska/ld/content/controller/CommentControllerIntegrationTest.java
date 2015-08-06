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

import java.util.List;

public class CommentControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_DOCUMENT = Content.RESOURCE_DOCUMENT;
    private static final String RESOURCE_COMMENT = Content.RESOURCE_COMMENT;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    Document document;

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
    }

    @Test
    public void testAddCommentToCommentHttpOk() throws Exception {
        //Add document
        HttpResponse response = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
        Document responseCreateDocument = ResponseHelper.getBody(response, Document.class);
        Assert.assertNotNull(responseCreateDocument);
        Assert.assertNotNull(responseCreateDocument.getId());

        //Add comment to the document
        String uriCommentDocument = RESOURCE_DOCUMENT + "/" + responseCreateDocument.getId() + "/comment";
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

        //create sub comment
        //Add comment to the existing comment
        String  uriAddSubComment = RESOURCE_COMMENT +"/" + responseCreateComment.getId() + "/comment";
        Comment subComment = new Comment();
        subComment.setText("Text");
        HttpResponse responseSubComment = UserSession.user().post(uriAddSubComment, subComment);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(responseSubComment));
        CommentDto commentDto = ResponseHelper.getBody(responseSubComment, CommentDto.class);
        Assert.assertNotNull(commentDto.getJsonParentId());
    }

}
