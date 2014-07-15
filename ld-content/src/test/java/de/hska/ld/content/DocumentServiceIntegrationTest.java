package de.hska.ld.content;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.core.AbstractIntegrationTest2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class DocumentServiceIntegrationTest extends AbstractIntegrationTest2 {

    @Autowired
    DocumentService documentService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(testUser);
    }

    @Test
    public void testSaveDocument() {
        Document document = new Document();
        document.setTitle("Title");
        document.setDescription("Description");

        Tag tag = new Tag();
        tag.setName("Tag");
        tag.setDescription("Description");
        document.getTagList().add(tag);

        Comment comment = new Comment();
        comment.setText("Text");
        document.getCommentList().add(comment);

        document = documentService.save(document);

        Assert.assertNotNull(document);
        Assert.assertNotNull(document.getId());
        Assert.assertNotNull(document.getCreator());
        Assert.assertNotNull(document.getCreatedAt());

        document.getCommentList().get(0).setText("Text (updated)");

        document = documentService.save(document);

        Assert.assertNotNull(document.getCommentList().get(0).getText().contains("(updated)"));
        Assert.assertNotNull(document.getModifiedAt());
    }
}
