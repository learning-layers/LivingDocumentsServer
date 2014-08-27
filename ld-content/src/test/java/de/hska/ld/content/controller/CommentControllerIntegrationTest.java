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
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
    }

    @Test
    public void testAddCommentToCommentHttpOk() {
        // Add document
        ResponseEntity<Document> response = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());

        // Add comment to the document
        String URI = RESOURCE_DOCUMENT + "/" + response.getBody().getId() + "/comment";
        Comment comment = new Comment();
        comment.setText("Text");
        HttpRequestWrapper request = post().resource(URI).asUser().body(comment);
        ResponseEntity<Comment> response2 = request.exec(Comment.class);
        Assert.assertEquals(HttpStatus.CREATED, response2.getStatusCode());

        // read document comments
        Map varMap = new HashMap<>();
        varMap.put("page-number", 0);
        varMap.put("page-size", 10);
        varMap.put("sort-direction", "DESC");
        varMap.put("sort-property", "createdAt");
        User user = userService.findByUsername("user");
        Map page = getPage(URI, user, varMap);
        Assert.assertNotNull(page);
        Assert.assertNotNull(page.containsKey("content"));
        Assert.assertTrue(((List) page.get("content")).size() > 0);

        // create sub comment
        // Add comment to the existing comment
        String URIAddCommentToComment = RESOURCE_COMMENT + "/" + response2.getBody().getId() + "/comment";
        Comment subComment = new Comment();
        subComment.setText("Text");
        HttpRequestWrapper requestAddCommentToComment = post().resource(URIAddCommentToComment).asUser().body(subComment);
        ResponseEntity<CommentDto> responseAddCommentToComment = requestAddCommentToComment.exec(CommentDto.class);
        Assert.assertEquals(HttpStatus.CREATED, responseAddCommentToComment.getStatusCode());
        Assert.assertNotNull(responseAddCommentToComment.getBody().getJsonParentId());
    }

}
