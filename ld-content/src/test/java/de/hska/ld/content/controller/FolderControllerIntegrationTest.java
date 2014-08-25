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

import de.hska.ld.content.persistence.domain.Folder;
import de.hska.ld.content.util.Content;
import de.hska.ld.core.AbstractIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class FolderControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_FOLDER = Content.RESOURCE_FOLDER;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void thatCreateFolderUsesHttpOkOnPersist() {
        Folder folder = new Folder("Test");
        ResponseEntity<Folder> response = post().resource(RESOURCE_FOLDER).asUser().body(folder).exec(Folder.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());
    }
}
