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

package de.hska.ld.core.controller;

import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.dto.NodeDto;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.JcrService;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class DocumentControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String DOCUMENT_RESOURCE = "/documents";

    @Autowired
    JcrService jcrService;

    @Autowired
    UserService userService;

    Session session;

    @Before
    public void setUp() throws Exception {
        User user = userService.findByUsername("user");
        session = jcrService.login(user);
    }

    @Test
    public void thatGetNodeMetaDataUsesHttpOkOnEntityLookupSuccess() throws RepositoryException {
        jcrService.createDocumentNode(session, "testDocument");

        ResponseEntity<NodeDto> response = exchange(DOCUMENT_RESOURCE + "/testDocument/meta", HttpMethod.GET,
                createUserHeader(), NodeDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
