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

package de.hska.ld.content.service;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.core.AbstractIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static de.hska.ld.content.ContentFixture.newDocument;

public class CommentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    CommentService commentService;

    @Autowired
    DocumentService documentService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(testUser);
    }

    @Test
    public void testSaveCommentWithContent() {
        Document document = documentService.save(newDocument());

        Comment comment = new Comment();
        comment.setParent(document);
        comment.setText("Test Text @admin ...");

        Comment savedComment = commentService.save(comment);

        Assert.assertNotNull(savedComment);
        Assert.assertNotNull(savedComment.getId());
        Assert.assertNotNull(savedComment.getCreator());
        Assert.assertNotNull(savedComment.getCreatedAt());

        savedComment.setText("NewTestText");
        Comment modifiedComment = commentService.save(savedComment);

        Assert.assertEquals("NewTestText", modifiedComment.getText());
        Assert.assertNotNull(modifiedComment.getModifiedAt());
    }

    @Test
    public void testReplyToComment() {
        Document document = documentService.save(newDocument());

        Comment comment = new Comment();
        comment.setParent(document);
        comment.setText("TestText");

        Comment savedComment = commentService.save(comment);

        Comment reply = new Comment();
        reply.setText("TestReplyText");

        reply = commentService.replyToComment(savedComment.getId(), reply);

        Assert.assertNotNull(reply);
        Assert.assertNotNull(reply.getId());
        Assert.assertNotNull(reply.getCreator());
        Assert.assertNotNull(reply.getCreatedAt());

        reply.setText("NewReplyTestText");
        Comment modifiedReply = commentService.save(reply);

        Assert.assertEquals("NewReplyTestText", modifiedReply.getText());
        Assert.assertNotNull(modifiedReply.getModifiedAt());
    }
}
