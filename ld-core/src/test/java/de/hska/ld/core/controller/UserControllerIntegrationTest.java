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
import de.hska.ld.core.dto.UserDto;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import static de.hska.ld.core.fixture.CoreFixture.*;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String USER_RESOURCE = "/users";

    @Autowired
    private UserService userService;

    @Test
    public void thatSaveUserUsesHttpCreatedOnPersist() {
        ResponseEntity<IdDto> response = exchange(USER_RESOURCE, HttpMethod.POST,
                createAdminHeader(newUserDto()), IdDto.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    public void thatSaveUserUsesHttpOkOnUpdate() {
        User user = userService.save(newUser(), PASSWORD);
        user.setFullName(user.getFullName() + " (updated)");

        String auth = user.getUsername() + ":" + PASSWORD;
        ResponseEntity<IdDto> response = exchange(USER_RESOURCE, HttpMethod.POST,
                createHeader(new UserDto(user, null), auth.getBytes()), IdDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void thatSaveUserAsAdminUsesHttpOkOnUpdate() {
        User user = userService.save(newUser(), PASSWORD);
        user.setFullName(user.getFullName() + " (updated)");

        ResponseEntity<IdDto> response = exchange(USER_RESOURCE, HttpMethod.POST,
                createAdminHeader(new UserDto(user, null)), IdDto.class);
        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void thatDeleteUserUsesHttpOkOnSuccess() {
        User user = userService.save(newUser(), PASSWORD);
        exchange(USER_RESOURCE + "/" + user.getId(), HttpMethod.DELETE, createAdminHeader(), IdDto.class);
    }

    @Test
    public void thatDeleteUserUsesHttpForbiddenOnAuthorizationFailure() {
        User user = userService.save(newUser(), PASSWORD);
        try {
            exchange(USER_RESOURCE + "/" + user.getId(), HttpMethod.DELETE, createUserHeader(), IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.FORBIDDEN, expectedClientException.getStatusCode());
    }

    @Test
    public void thatDeleteUserUsesHttpNotFoundOnEntityLookupFailure() {
        try {
            exchange(USER_RESOURCE + "/" + -1, HttpMethod.DELETE, createAdminHeader(), IdDto.class);
        } catch (HttpStatusCodeException e) {
            expectedClientException = e;
        }
        Assert.assertNotNull(expectedClientException);
        Assert.assertEquals(HttpStatus.NOT_FOUND, expectedClientException.getStatusCode());
    }
}
