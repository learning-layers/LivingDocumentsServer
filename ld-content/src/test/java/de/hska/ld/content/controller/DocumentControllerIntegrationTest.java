package de.hska.ld.content.controller;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.AbstractIntegrationTest2;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static de.hska.ld.core.fixture.CoreFixture.newUser;

public class DocumentControllerIntegrationTest extends AbstractIntegrationTest2 {

    private static final String RESOURCE_API = "/api";
    private static final String RESOURCE_DOCUMENT = "/document";
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    @Autowired
    UserService userService;

    Document document;

    @Before
    public void setUp() throws Exception {
        User user = userService.findByUsername("user");
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
    }

    @Test
    public void thatCreateDocumentUsesHttpOkOnPersist() {
        ResponseEntity<Document> response = post().resource(RESOURCE_API + RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());
    }
}
