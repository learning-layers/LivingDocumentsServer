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
import de.hska.ld.core.exception.ApplicationError;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import static de.hska.ld.core.fixture.CoreFixture.PASSWORD;
import static de.hska.ld.core.fixture.CoreFixture.newUser;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_USER = Core.RESOURCE_USER;

    @Autowired
    private UserService userService;

    @Test
    public void testSaveUserUsesHttpCreatedOnPersist() {
        ResponseEntity<User> response = post().resource(RESOURCE_USER).body(newUser()).asAdmin().exec(User.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void testSaveUserUsesHttpOkOnUpdate() {
        User user = userService.save(newUser());
        user.setFullName(user.getFullName() + " (updated)");

        ResponseEntity<User> response = post().resource(RESOURCE_USER).asAdmin().body(user).exec(User.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testSaveUserAsAdminUsesHttpOkOnUpdate() {
        User user = userService.save(newUser());
        user.setFullName(user.getFullName() + " (updated)");

        ResponseEntity<User> response = post().resource(RESOURCE_USER).asAdmin().body(user).exec(User.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testDeleteUserUsesHttpOkOnSuccess() {
        User user = userService.save(newUser());
        delete().resource(RESOURCE_USER + "/" + user.getId()).asAdmin().exec(IdDto.class);
    }

    @Test
    public void testDeleteUserUsesHttpForbiddenOnAuthorizationFailure() {
        User user = userService.save(newUser());
        try {
            delete().resource(RESOURCE_USER + "/" + user.getId()).asUser().exec(IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.FORBIDDEN, expectedClientException.getStatusCode());
    }

    @Test
    public void testUpdateUsernameUsesHttpConflictIfAlreadyExists() {
        User user1 = userService.save(newUser());
        User user2 = userService.save(newUser());

        byte[] authUser2 = (user2.getUsername() + ":" + PASSWORD).getBytes();
        user2.setUsername(user1.getUsername());
        try {
            post().resource(RESOURCE_USER).as(authUser2).body(user2).asAdmin().exec(ApplicationError.class);
        } catch (HttpStatusCodeException e) {
            parseApplicationError(e.getResponseBodyAsString());
            expectedClientException = e;
        }

        Assert.assertNotNull(applicationError);
        Assert.assertTrue(applicationError.getField().equals("username"));
        Assert.assertTrue(applicationError.getKey().equals("ALREADY_EXISTS"));
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.CONFLICT, expectedClientException.getStatusCode());
    }

    @Test
    public void testDeleteUserUsesHttpNotFoundOnEntityLookupFailure() {
        try {
            delete().resource(RESOURCE_USER + "/" + -1).asAdmin().exec(IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.NOT_FOUND, expectedClientException.getStatusCode());
    }

    @Test
    public void testAuthenticateDoesNotContainPasswordHashInResponseBody() {
        ResponseEntity<User> response = get().asUser().resource(RESOURCE_USER + "/authenticate").exec(User.class);
        User user = response.getBody();
        Assert.assertNull(user.getPassword());
    }
}
