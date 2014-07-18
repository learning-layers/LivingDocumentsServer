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

package de.hska.ld.content;

import de.hska.ld.content.persistence.domain.Attachment;
import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.TagService;
import de.hska.ld.core.AbstractIntegrationTest2;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.InputStream;

import static de.hska.ld.content.ContentFixture.newComment;
import static de.hska.ld.content.ContentFixture.newDocument;

public class DocumentServiceIntegrationTest extends AbstractIntegrationTest2 {

    static final String TEST_PDF = "test.pdf";

    @Autowired
    DocumentService documentService;

    @Autowired
    TagService tagService;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        setAuthentication(testUser);
    }

    @Test
    public void testSaveDocumentWithContent() {
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

        InputStream in = DocumentServiceIntegrationTest.class.getResourceAsStream("/" + TEST_PDF);
        Attachment attachment = new Attachment(in, TEST_PDF);
        document.getAttachmentList().add(attachment);

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

    @Test
    public void testMarkDocumentAsDeleted() {
        Document document = documentService.save(newDocument());
        Assert.assertFalse(document.isDeleted());
        documentService.markAsDeleted(document.getId());
        document = documentService.findById(document.getId());
        Assert.assertTrue(document.isDeleted());
    }

    @Test
    public void testAddComment() {
        Document document = documentService.save(newDocument());
        Assert.assertNotNull(document);

        Comment comment = documentService.addComment(document.getId(), newComment());

        document = documentService.findById(document.getId());
        Assert.assertNotNull(document);

        document = documentService.loadContentCollection(document, Comment.class);
        Assert.assertTrue(document.getCommentList().contains(comment));
    }

    @Test
    public void testAddTag() {
        Document document = documentService.save(newDocument());
        Assert.assertNotNull(document);

        Tag tag = tagService.createTag(TagServiceIntegrationTest.TAG_NAME1, TagServiceIntegrationTest.TAG_DESCRIPTION1);

        documentService.addTag(document.getId(), tag);

        document = documentService.findById(document.getId());
        Assert.assertNotNull(document);

        document = documentService.loadContentCollection(document, Tag.class);
        Assert.assertTrue(document.getTagList().contains(tag));
    }
}
