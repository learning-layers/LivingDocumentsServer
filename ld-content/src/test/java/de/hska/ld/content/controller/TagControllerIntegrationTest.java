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

import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.TagService;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.UserSession;
import de.hska.ld.core.service.UserService;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

public class TagControllerIntegrationTest extends AbstractIntegrationTest {

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
        super.setUp();
        setAuthentication(testUser);
        userService.findByUsername("user");
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
        tag = new Tag();
        tag.setName("tagName");
        tag.setDescription("tagDescription");
    }

    @Test
    public void testCreateTagUsesHttpCreatedOnPersist() throws Exception {
        HttpResponse response = UserSession.user().post(RESOURCE_TAG, tag);

        Assert.assertEquals(HttpStatus.CREATED, UserSession.getStatusCode(response));

        Tag tag = UserSession.getBody(response, Tag.class);
        Assert.assertNotNull(tag.getId());
    }

    @Test
    public void testEditTagUsesHttpOkOnPersist() throws Exception {
        HttpResponse response = UserSession.user().post(RESOURCE_TAG, tag);
        Assert.assertEquals(HttpStatus.CREATED, UserSession.getStatusCode(response));
        Tag tag = UserSession.getBody(response, Tag.class);
        Assert.assertNotNull(tag.getId());

        String updatedName = "updatedName";
        tag.setName(updatedName);
        HttpResponse response2 = UserSession.user().put(RESOURCE_TAG + "/" + tag.getId(), tag);
        Assert.assertEquals(HttpStatus.OK, UserSession.getStatusCode(response2));
        Tag tag2 = UserSession.getBody(response2, Tag.class);
        Assert.assertNotNull(tag2.getId());
        Assert.assertEquals(updatedName, tag2.getName());
    }

}
