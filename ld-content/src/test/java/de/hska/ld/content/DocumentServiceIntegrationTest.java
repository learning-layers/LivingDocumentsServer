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

import de.hska.ld.content.persistence.domain.*;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.TagService;
import de.hska.ld.core.AbstractIntegrationTest2;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import java.io.InputStream;

import static de.hska.ld.content.ContentFixture.*;
import static de.hska.ld.core.fixture.CoreFixture.newUser;

public class DocumentServiceIntegrationTest extends AbstractIntegrationTest2 {

    static final String TEST_PDF = "test.pdf";

    @Autowired
    DocumentService documentService;

    @Autowired
    TagService tagService;

    @Autowired
    UserService userService;

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

        InputStream in = DocumentServiceIntegrationTest.class.getResourceAsStream("/" + TEST_PDF);
        Attachment attachment = new Attachment(in, TEST_PDF);
        document.getAttachmentList().add(attachment);

        document = documentService.save(document);

        Assert.assertNotNull(document);
        Assert.assertNotNull(document.getId());
        Assert.assertNotNull(document.getCreator());
        Assert.assertNotNull(document.getCreatedAt());
        Assert.assertNull(document.getModifiedAt());

        document.setTitle(document.getTitle() + " (updated)");
        document = documentService.save(document);

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

        Tag tag = tagService.save(newTag());

        User userWithoutAccess = userService.save(newUser());
        setAuthentication(userWithoutAccess);
        UserNotAuthorizedException userNotAuthorizedException = null;
        try {
            documentService.addTag(document.getId(), tag);
        } catch (UserNotAuthorizedException e) {
            userNotAuthorizedException = e;
        }
        Assert.assertNotNull(userNotAuthorizedException);

        setAuthentication(testUser);
        documentService.addTag(document.getId(), tag);

        document = documentService.findById(document.getId());
        Assert.assertNotNull(document);

        document = documentService.loadContentCollection(document, Tag.class);
        Assert.assertTrue(document.getTagList().contains(tag));
    }

    @Test
    public void testDocumentPageAccessControl() {
        User userWithAccess = userService.save(newUser());
        User userWithoutAccess = userService.save(newUser());

        Document document = documentService.save(newDocument());

        documentService.addAccess(document, userWithAccess, Access.Permission.READ);

        setAuthentication(userWithAccess);
        Page<Document> documentPage = documentService.getDocumentsPage(0, 10, "DESC", "createdAt");

        Assert.assertNotNull(documentPage);
        Assert.assertTrue(documentPage.getNumberOfElements() == 1);

        setAuthentication(userWithoutAccess);
        documentPage = documentService.getDocumentsPage(0, 10, "DESC", "createdAt");

        Assert.assertNotNull(documentPage);
        Assert.assertTrue(documentPage.getNumberOfElements() == 0);
    }

    @Test
    public void testAddAndRemoveAccessFromDocument() {
        Document document = documentService.save(newDocument());

        User user = userService.save(newUser());
        setAuthentication(user);

        Page<Document> documentPage = documentService.getDocumentsPage(0, 10, "DESC", "createdAt");
        Assert.assertTrue(documentPage.getNumberOfElements() == 0);

        setAuthentication(testUser);
        document = documentService.addAccess(document, user, Access.Permission.READ);

        setAuthentication(user);
        documentPage = documentService.getDocumentsPage(0, 10, "DESC", "createdAt");
        Assert.assertTrue(documentPage.getNumberOfElements() == 1);

        setAuthentication(testUser);
        document = documentService.removeAccess(document, user, Access.Permission.READ);

        setAuthentication(user);
        documentPage = documentService.getDocumentsPage(0, 10, "DESC", "createdAt");
        Assert.assertTrue(documentPage.getNumberOfElements() == 0);

        document.setTitle(document.getTitle() + "(updated)");
        UserNotAuthorizedException userNotAuthorizedException = null;
        try {
            documentService.save(document);
        } catch (UserNotAuthorizedException e) {
            userNotAuthorizedException = e;
        }
        Assert.assertNotNull(userNotAuthorizedException);

        setAuthentication(testUser);
        document = documentService.addAccess(document, user, Access.Permission.READ, Access.Permission.WRITE);

        setAuthentication(user);
        document.setTitle(document.getTitle() + "(updated)");
        document = documentService.save(document);

        Assert.assertTrue(document.getTitle().contains("(updated)"));
    }

    @Test
    public void testAddAndRemoveSubscription() {
        Document document = documentService.save(newDocument());

        User user = userService.save(newUser());
        document = documentService.addSubscription(document.getId(), user, Subscription.Type.COMMENT, Subscription.Type.DISCUSSION);

        Assert.assertNotNull(document.getSubscriptionList());
        Assert.assertTrue(document.getSubscriptionList().size() == 1);
        Assert.assertTrue(document.getSubscriptionList().get(0).getTypeList().size() == 2);
        Assert.assertEquals(document.getSubscriptionList().get(0).getTypeList().get(0), Subscription.Type.COMMENT);
        Assert.assertEquals(document.getSubscriptionList().get(0).getTypeList().get(1), Subscription.Type.DISCUSSION);

        document = documentService.removeSubscription(document.getId(), user, Subscription.Type.COMMENT);
        Assert.assertNotNull(document.getSubscriptionList());
        Assert.assertTrue(document.getSubscriptionList().size() == 1);
        Assert.assertTrue(document.getSubscriptionList().get(0).getTypeList().size() == 1);
        Assert.assertEquals(document.getSubscriptionList().get(0).getTypeList().get(0), Subscription.Type.DISCUSSION);
    }
}
