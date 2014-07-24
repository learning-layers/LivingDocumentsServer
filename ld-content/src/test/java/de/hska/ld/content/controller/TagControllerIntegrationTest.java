package de.hska.ld.content.controller;

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.TagService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.AbstractIntegrationTest2;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TagControllerIntegrationTest extends AbstractIntegrationTest2 {

    private static final String RESOURCE_DOCUMENT = Content.RESOURCE_DOCUMENT;
    private static final String RESOURCE_TAG = Content.RESOURCE_TAG;
    private static final String RESOURCE_COMMENT = "/comment";
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    @Autowired
    DocumentService documentService;

    @Autowired
    UserService userService;

    @Autowired
    TagService tagService;

    Document document;

    Tag tag;

    @Before
    public void setUp() throws Exception {
        User user = userService.findByUsername("user");
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
        tag = new Tag();
        tag.setName("tagName");
        tag.setDescription("tagDescription");
    }

    @Test
    public void thatCreateTagUsesHttpCreatedOnPersist() {
        ResponseEntity<Tag> response = post().resource(RESOURCE_TAG).asUser().body(tag).exec(Tag.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());
    }

    @Test
    public void thatEditTagUsesHttpOkOnPersist() {
        ResponseEntity<Tag> responseCreate = post().resource(RESOURCE_TAG).asUser().body(tag).exec(Tag.class);
        Assert.assertEquals(HttpStatus.CREATED, responseCreate.getStatusCode());
        Long tagId = responseCreate.getBody().getId();
        Assert.assertNotNull(tagId);

        String updatedName = "updatedName";
        tag.setName(updatedName);
        ResponseEntity<Tag> responseUpdate = put().resource(RESOURCE_TAG + "/" + tagId).asUser().body(tag).exec(Tag.class);
        Assert.assertEquals(HttpStatus.OK, responseUpdate.getStatusCode());
        Assert.assertNotNull(responseUpdate.getBody().getId());
        Assert.assertEquals(updatedName, responseUpdate.getBody().getName());
    }

}
