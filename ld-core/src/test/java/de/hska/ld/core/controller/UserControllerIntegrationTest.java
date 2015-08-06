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
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static de.hska.ld.core.util.CoreUtil.newUser;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_USER = Core.RESOURCE_USER;

    @Autowired
    private UserService userService;

    @Test
    public void testSaveUserUsesHttpCreatedOnPersist() throws Exception {
        HttpResponse response = UserSession.admin().post(RESOURCE_USER, newUser());
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testSaveUserUsesHttpOkOnUpdate() throws Exception {
        User user = userService.save(newUser());

        user.setFullName(user.getFullName() + " (updated)");
        HttpResponse response = UserSession.admin().put(RESOURCE_USER + "/" + user.getId(), user);
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(response));
        User updatedUser = ResponseHelper.getBody(response, User.class);
        Assert.assertNotNull(updatedUser);
        Assert.assertNotNull(updatedUser.getFullName());
        Assert.assertEquals(user.getFullName(), updatedUser.getFullName());
    }

    @Test
    public void testDeleteUserUsesHttpOkOnSuccess() throws Exception {
        User user = userService.save(newUser());

        HttpResponse response = UserSession.admin().delete(RESOURCE_USER + "/" + user.getId());
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(response));
        Long deletedUserId = ResponseHelper.getBody(response, Long.class);
        Assert.assertEquals(user.getId(), deletedUserId);
    }

    @Test
    public void testDeleteUserUsesHttpForbiddenOnAuthorizationFailure() throws Exception {
        User user = userService.save(newUser());

        HttpResponse response = UserSession.user().delete(RESOURCE_USER + "/" + user.getId());
        Assert.assertEquals(HttpStatus.FORBIDDEN, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testUpdateUsernameUsesHttpConflictIfAlreadyExists() throws Exception {
        User user = newUser();

        HttpResponse response = UserSession.admin().post(RESOURCE_USER, user);
        Assert.assertEquals(HttpStatus.CREATED, ResponseHelper.getStatusCode(response));

        HttpResponse response2 = UserSession.admin().post(RESOURCE_USER, user);
        Assert.assertEquals(HttpStatus.CONFLICT, ResponseHelper.getStatusCode(response2));
    }

    @Test
    public void testDeleteUserUsesHttpNotFoundOnEntityLookupFailure() throws Exception {
        String invalidUserURL = RESOURCE_USER + "/" + -1;
        HttpResponse response = UserSession.admin().delete(invalidUserURL);
        Assert.assertEquals(HttpStatus.NOT_FOUND, ResponseHelper.getStatusCode(response));
    }

    @Test
    public void testAuthenticateDoesNotContainPasswordHashInResponseBody() throws Exception {
        HttpResponse response = UserSession.user().get(RESOURCE_USER + "/authenticate");
        Assert.assertEquals(HttpStatus.OK, ResponseHelper.getStatusCode(response));
        User user = ResponseHelper.getBody(response, User.class);
        Assert.assertNotNull(user);
        Assert.assertNull(user.getPassword());
    }
}
