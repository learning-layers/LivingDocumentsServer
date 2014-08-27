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
import de.hska.ld.core.dto.IdDto;
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.util.Core;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.UUID;

import static de.hska.ld.core.fixture.CoreFixture.newRole;

public class RoleControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_ROLE = Core.RESOURCE_ROLE;

    @Autowired
    RoleService roleService;

    @Test
    public void testSaveRoleUsesHttpCreatedOnPersist() {
        ResponseEntity<IdDto> response = post().asAdmin().resource(RESOURCE_ROLE).body(newRole()).exec(IdDto.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testSaveRoleUsesHttpOkOnUpdate() {
        Role role = roleService.save(newRole());
        role.setName(UUID.randomUUID().toString());

        ResponseEntity<IdDto> response = post().asAdmin().resource(RESOURCE_ROLE).body(role).exec(IdDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testSaveRoleUsesHttpForbiddenOnAuthorizationFailure() {
        try {
            post().asUser().resource(RESOURCE_ROLE).body(newRole()).exec(IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.FORBIDDEN, expectedClientException.getStatusCode());
    }

    @Test
    public void testDeleteRoleUsesHttpOkOnSuccess() {
        Role role = roleService.save(newRole());
        delete().asAdmin().resource(RESOURCE_ROLE + "/" + role.getId()).exec(IdDto.class);
    }

    @Test
    public void testDeleteRoleUsesHttpForbiddenOnAuthorizationFailure() {
        Role role = roleService.save(newRole());
        try {
            delete().asUser().resource(RESOURCE_ROLE + "/" + role.getId()).exec(IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.FORBIDDEN, expectedClientException.getStatusCode());
    }

    @Test
    public void testDeleteRoleUsesHttpNotFoundOnEntityLookupFailure() {
        try {
            delete().asAdmin().resource(RESOURCE_ROLE + "/" + -1).exec(IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
            parseApplicationError(e.getResponseBodyAsString());
        }
        Assert.assertNotNull(applicationError);
        Assert.assertTrue(applicationError.getField().equals("id"));
        Assert.assertTrue(applicationError.getKey().equals("NOT_FOUND"));
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.NOT_FOUND, expectedClientException.getStatusCode());
    }
}
