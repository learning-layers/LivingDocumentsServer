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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.UserSession;
import de.hska.ld.core.dto.IdDto;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static de.hska.ld.core.util.CoreUtil.newUser;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_USER = Core.RESOURCE_USER;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSaveUserUsesHttpCreatedOnPersist() {
        UserSession adminSession = new UserSession();
        try {
            adminSession.loginAsAdmin();
        } catch (Exception e) {
            Assert.fail();
        }

        try {
            HttpResponse response = adminSession.postJson(RESOURCE_USER, newUser());
            Assert.assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
        } catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void testSaveUserUsesHttpOkOnUpdate() {
        User user = userService.save(newUser());
        user.setFullName(user.getFullName() + " (updated)");

        UserSession adminSession = new UserSession();
        try {
            adminSession.loginAsAdmin();
        } catch (Exception e) {
            Assert.fail();
        }

        try {
            HttpResponse response = adminSession.postJson(RESOURCE_USER, user);
            Assert.assertEquals(HttpStatus.OK, HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
        } catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void testDeleteUserUsesHttpOkOnSuccess() {
        User user = userService.save(newUser());

        UserSession adminSession = new UserSession();
        try {
            adminSession.loginAsAdmin();
        } catch (Exception e) {
            Assert.fail();
        }

        try {
            HttpResponse response = adminSession.delete(RESOURCE_USER + "/" + user.getId(), null);
            Assert.assertEquals(HttpStatus.OK, HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
            HttpEntity entity = response.getEntity();
            String body = IOUtils.toString(entity.getContent(), Charset.forName("UTF-8"));
            Assert.assertEquals(user.getId(), Long.valueOf(body));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testDeleteUserUsesHttpForbiddenOnAuthorizationFailure() {
        User user = userService.save(newUser());

        UserSession userSession = new UserSession();
        try {
            userSession.loginAsUser();
        } catch (Exception e) {
            Assert.fail();
        }

        try {
            HttpResponse response = userSession.delete(RESOURCE_USER + "/" + user.getId(), null);
            Assert.assertEquals(HttpStatus.FORBIDDEN, HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testUpdateUsernameUsesHttpConflictIfAlreadyExists() {
        User user1 = newUser();

        UserSession adminSession = new UserSession();
        try {
            adminSession.loginAsAdmin();
        } catch (Exception e) {
            Assert.fail();
        }

        try {
            HttpResponse response = adminSession.postJson(RESOURCE_USER, user1);
            Assert.assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
        } catch (IOException e) {
            Assert.fail();
        }

        try {
            HttpResponse response = adminSession.postJson(RESOURCE_USER, user1);
            Assert.assertEquals(HttpStatus.CONFLICT, HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
        } catch (IOException e) {
            Assert.fail();
        }
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
