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
import de.hska.ld.core.ResponseHelper;
import de.hska.ld.core.UserSession;
import de.hska.ld.core.dto.IdDto;
import de.hska.ld.core.persistence.domain.Role;
import de.hska.ld.core.service.RoleService;
import de.hska.ld.core.util.Core;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static de.hska.ld.core.util.CoreUtil.newRole;

public class RoleControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_ROLE = Core.RESOURCE_ROLE;

    @Autowired
    RoleService roleService;

    @Test
    public void testSaveRoleUsesHttpCreatedOnPersist() throws Exception {

        HttpResponse response = UserSession.admin().post(RESOURCE_ROLE, newRole());
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
        Role createdRole = ResponseHelper.getBody(response, Role.class);
        Assert.assertNotNull(createdRole);
        Assert.assertNotNull(createdRole.getId());
    }

    @Test
    public void testSaveRoleUsesHttpOkOnUpdate() throws Exception {
        Role role = roleService.save(newRole());
        role.setName(UUID.randomUUID().toString());

        HttpResponse response = UserSession.admin().post(RESOURCE_ROLE, role);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(response));
        IdDto idDto = ResponseHelper.getBody(response, IdDto.class);
        Assert.assertNotNull(idDto.getId());
    }

    @Test
    public void testSaveRoleUsesHttpForbiddenOnAuthorizationFailure() throws Exception {

        HttpResponse response = UserSession.user().post(RESOURCE_ROLE, newRole());
        Assert.assertEquals(HttpStatus.FORBIDDEN, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testDeleteRoleUsesHttpOkOnSuccess() throws Exception {
        Role role = roleService.save(newRole());

        HttpResponse response = UserSession.admin().delete(RESOURCE_ROLE + "/" + role.getId(), null);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testDeleteRoleUsesHttpForbiddenOnAuthorizationFailure() throws Exception {
        Role role = roleService.save(newRole());

        HttpResponse response = UserSession.user().delete(RESOURCE_ROLE + "/" + role.getId(), null);
        Assert.assertEquals(HttpStatus.FORBIDDEN, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testDeleteRoleUsesHttpNotFoundOnEntityLookupFailure() throws Exception {

        HttpResponse response = UserSession.admin().delete(RESOURCE_ROLE + "/" + -1, null);
        Assert.assertEquals(HttpStatus.NOT_FOUND, ResponseHelper.getStatusCode(response));
    }
}
