package de.hska.ld.content.controller;

import de.hska.ld.content.persistence.domain.Comment;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
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
import org.springframework.web.client.HttpClientErrorException;

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
    public void thatCreateDocumentUsesHttpOkOnPersist() {
        ResponseEntity<Document> response = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());
    }

    @Test
    public void testGETDocumentPageHttpOk() {
        ResponseEntity<Document> response = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());

        //User admin = userService.findByUsername(Core.BOOTSTRAP_ADMIN);

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
    public void testRemoveDocumentHttpOk() {
        // Add document
        ResponseEntity<Document> response = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());

        // Remove document
        String URI = RESOURCE_DOCUMENT + "/" + response.getBody().getId();
        ResponseEntity response2 = delete().resource(URI).asUser().exec();
        Assert.assertEquals(HttpStatus.OK, response2.getStatusCode());


        boolean exceptionOccured = false;
        try {
            ResponseEntity<Document> response3 = get().resource(URI).asUser().exec(Document.class);
        } catch (HttpClientErrorException e) {
            Assert.assertTrue(e.toString().contains("404 Not Found"));
            exceptionOccured = true;
        }
        if (!exceptionOccured) {
            Assert.fail();
        }
    }

    @Test
    public void testAddCommentHttpOk() {
        // Add document
        ResponseEntity<Document> response = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());

        // Add comment to the document
        String URI = RESOURCE_DOCUMENT + "/" + response.getBody().getId() + RESOURCE_COMMENT;
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
        Map page = getPage(URI, testUser, varMap);
        Assert.assertNotNull(page);
        Assert.assertNotNull(page.containsKey("content"));
        Assert.assertTrue(((List) page.get("content")).size() > 0);
    }

    @Test
    public void testAddTagHttpOk() {
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
        // TODO

    }

}
