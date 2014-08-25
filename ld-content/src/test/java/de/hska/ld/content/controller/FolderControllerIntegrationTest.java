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

import de.hska.ld.content.dto.FolderDto;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Folder;
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

public class FolderControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_FOLDER = Content.RESOURCE_FOLDER;
    private static final String RESOURCE_DOCUMENT = Content.RESOURCE_DOCUMENT;
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    Document document;

    @Autowired
    UserService userService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
    }

    @Test
    public void thatCreateFolderUsesHttpCreatedOnPersist() {
        Folder folder = new Folder("Test");
        ResponseEntity<Folder> response = post().resource(RESOURCE_FOLDER).asUser().body(folder).exec(Folder.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());
    }

    @Test
    public void thatCreateSubFolderUsesHttpCreatedOnPersist() {
        Folder folder = new Folder("Test");
        ResponseEntity<Folder> response = post().resource(RESOURCE_FOLDER).asUser().body(folder).exec(Folder.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Long folderId = response.getBody().getId();
        Assert.assertNotNull(folderId);

        Folder subFolder = new Folder("Sub Test");
        ResponseEntity<FolderDto> response2 = post().resource(RESOURCE_FOLDER + "/" + folderId + "/folders").asUser().body(subFolder).exec(FolderDto.class);
        Assert.assertEquals(HttpStatus.CREATED, response2.getStatusCode());
        Assert.assertNotNull(response2.getBody().getJsonParentId());
    }

    @Test
    public void thatAddDocumentToFolderUsesHttpOkOnPersist() {
        Folder folder = new Folder("Test");
        ResponseEntity<Folder> response = post().resource(RESOURCE_FOLDER).asUser().body(folder).exec(Folder.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Long folderId = response.getBody().getId();
        Assert.assertNotNull(folderId);

        ResponseEntity<Document> responseDocument = post().resource(RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, responseDocument.getStatusCode());
        Long documentId = responseDocument.getBody().getId();
        Assert.assertNotNull(documentId);

        ResponseEntity<FolderDto> responseAddDocument = post().resource(RESOURCE_FOLDER + "/" + folderId + "/documents/" + documentId).asUser().exec(FolderDto.class);
        Assert.assertEquals(HttpStatus.OK, responseAddDocument.getStatusCode());

        // TODO add check if the document is in the parent folder
    }

    @Test
    public void thatShareFolderWithOtherUsersUsesHttpOkOnPersist() {
        Folder folder = new Folder("Test");
        ResponseEntity<Folder> response = post().resource(RESOURCE_FOLDER).asUser().body(folder).exec(Folder.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Long folderId = response.getBody().getId();
        Assert.assertNotNull(folderId);

        User adminUser = userService.findByUsername("admin");
        //setAuthentication(adminUser);

        ResponseEntity<FolderDto> responseAddDocument = post()
                .resource(RESOURCE_FOLDER + "/" + folderId + "/share" + "?users=" + adminUser.getId() + "&permissions=" + "WRITE;READ") // TODO add "READ" too
                .asUser().exec(FolderDto.class);
        Assert.assertEquals(HttpStatus.OK, responseAddDocument.getStatusCode());
    }

}