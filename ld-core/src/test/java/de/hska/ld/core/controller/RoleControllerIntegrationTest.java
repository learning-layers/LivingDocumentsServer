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
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.UUID;

import static de.hska.ld.core.fixture.CoreFixture.newRole;

public class RoleControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ROLE_RESOURCES = "/roles";

    @Autowired
    RoleService roleService;

    @Test
    public void thatSaveRoleUsesHttpCreatedOnPersist() {
        ResponseEntity<IdDto> response = exchange(ROLE_RESOURCES, HttpMethod.POST,
                createAdminHeader(newRole()), IdDto.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void thatSaveRoleUsesHttpOkOnUpdate() {
        Role role = roleService.save(newRole());

        role.setName(UUID.randomUUID().toString());
        ResponseEntity<IdDto> response = exchange(ROLE_RESOURCES, HttpMethod.POST,
                createAdminHeader(role), IdDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void thatSaveRoleUsesHttpForbiddenOnAuthorizationFailure() {
        try {
            exchange(ROLE_RESOURCES, HttpMethod.POST, createUserHeader(newRole()), IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.FORBIDDEN, expectedClientException.getStatusCode());
    }

    @Test
    public void thatDeleteRoleUsesHttpOkOnSuccess() {
        Role role = roleService.save(newRole());
        exchange(ROLE_RESOURCES + "/" + role.getId(), HttpMethod.DELETE, createAdminHeader(), IdDto.class);
    }

    @Test
    public void thatDeleteRoleUsesHttpForbiddenOnAuthorizationFailure() {
        Role role = roleService.save(newRole());
        try {
            exchange(ROLE_RESOURCES + "/" + role.getId(), HttpMethod.DELETE, createUserHeader(), IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.FORBIDDEN, expectedClientException.getStatusCode());
    }

    @Test
    public void thatDeleteRoleUsesHttpNotFoundOnEntityLookupFailure() {
        try {
            exchange(ROLE_RESOURCES + "/" + -1, HttpMethod.DELETE, createAdminHeader(), IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.NOT_FOUND, expectedClientException.getStatusCode());
    }
}
