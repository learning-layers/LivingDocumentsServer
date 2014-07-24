package de.hska.ld.content;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.service.CommentService;
import de.hska.ld.core.AbstractIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CommentServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    CommentService commentService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(testUser);
    }

    @Test
    public void testSaveCommentWithContent() {
        Comment comment = new Comment();
        comment.setText("TestText");

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
        Comment comment = new Comment();
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
