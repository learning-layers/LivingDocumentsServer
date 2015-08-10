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
import de.hska.ld.core.ResponseHelper;
import de.hska.ld.core.UserSession;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

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
        setAuthentication(testUser);
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
    }

    @Test
    public void testCreateFolderUsesHttpCreatedOnPersist() throws Exception {
        Folder folder = new Folder("Test");
        HttpResponse response = UserSession.user().post(RESOURCE_FOLDER, folder);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
        folder = ResponseHelper.getBody(response, Folder.class);
        Assert.assertNotNull(folder.getId());
    }

    @Test
    public void testCreateSubFolderUsesHttpCreatedOnPersist() throws Exception {
        Folder folder = new Folder("Test");
        HttpResponse response = UserSession.user().post(RESOURCE_FOLDER, folder);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
        folder = ResponseHelper.getBody(response, Folder.class);
        Long folderId = folder.getId();
        Assert.assertNotNull(folderId);

        Folder subFolder = new Folder("Sub Test");
        HttpResponse response2 = UserSession.user().post(RESOURCE_FOLDER + "/" + folderId + "/folders", subFolder);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response2));
        Assert.assertEquals(ResponseHelper.getBody(response2, FolderDto.class).getJsonParentId(), folderId);
    }

    @Test
    public void testAddDocumentToFolderUsesHttpOkOnPersist() throws Exception {
        Folder folder = new Folder("Test");
        HttpResponse responseFolder = UserSession.user().post(RESOURCE_FOLDER, folder);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(responseFolder));
        folder = ResponseHelper.getBody(responseFolder, Folder.class);
        Assert.assertNotNull(folder);
        Long folderId = folder.getId();
        Assert.assertNotNull(folderId);

        HttpResponse responseDocument = UserSession.user().post(RESOURCE_DOCUMENT, document);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(responseDocument));
        document = ResponseHelper.getBody(responseDocument, Document.class);
        Assert.assertNotNull(document);
        Long documentId = document.getId();
        Assert.assertNotNull(documentId);

        HttpResponse responseDocInFolder = UserSession.user().post(RESOURCE_FOLDER + "/" + folderId + "/documents/" + documentId + "?old-parent=-1", document);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseDocInFolder));

        // TODO add check if the document is in the parent folder
    }

    @Test
    public void testShareFolderWithOtherUsersUsesHttpOkOnPersist() throws Exception {
        Folder folder = new Folder("Test");
        HttpResponse responseFolder = UserSession.user().post(RESOURCE_FOLDER, folder);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(responseFolder));
        folder = ResponseHelper.getBody(responseFolder, Folder.class);
        Assert.assertNotNull(folder);
        Long folderId = folder.getId();
        Assert.assertNotNull(folderId);

        User adminUser = userService.findByUsername("admin");

        HttpResponse responseShareFolder = UserSession.user().post(RESOURCE_FOLDER + "/" + folderId + "/share" + "?users=" + adminUser.getId() + "&permissions=" + "WRITE;READ", folder);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseShareFolder));

        folder = ResponseHelper.getBody(responseShareFolder, FolderDto.class);
        Assert.assertNotNull(folder);
        Assert.assertNotNull(folderId);

    }

    @Test
    public void testRevokeShareFolderHttpOKOnPersist() throws Exception {

        Folder folder = new Folder("Test");
        HttpResponse responseFolder = UserSession.user().post(RESOURCE_FOLDER, folder);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(responseFolder));
        folder = ResponseHelper.getBody(responseFolder, Folder.class);
        Assert.assertNotNull(folder);
        Long folderId = folder.getId();
        Assert.assertNotNull(folderId);

        User adminUser = userService.findByUsername("admin");

        HttpResponse responseShareFolder = UserSession.user().post(RESOURCE_FOLDER + "/" + folderId + "/share" + "?users=" + adminUser.getId() + "&permissions=" + "WRITE;READ", folder);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseShareFolder));
        folder = ResponseHelper.getBody(responseShareFolder, Folder.class);
        Assert.assertNotNull(folder);
        Assert.assertNotNull(folderId);

        HttpResponse responseRevokeShareFolder = UserSession.user().post(RESOURCE_FOLDER + "/" + folderId + "/share/revoke" + "?users=" + adminUser.getId() + "&permissions=" + "WRITE;READ", folder);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(responseRevokeShareFolder));
        folder = ResponseHelper.getBody(responseRevokeShareFolder, Folder.class);
        Assert.assertNotNull(folder);
        Assert.assertNotNull(folderId);
    }
}
