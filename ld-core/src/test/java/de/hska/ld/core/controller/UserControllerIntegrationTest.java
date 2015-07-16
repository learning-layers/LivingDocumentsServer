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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.dto.IdDto;
import de.hska.ld.core.exception.ApplicationError;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import de.hska.ld.core.util.Core;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.ArrayList;
import java.util.List;

import static de.hska.ld.core.fixture.CoreFixture.PASSWORD;
import static de.hska.ld.core.fixture.CoreFixture.newUser;

public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String RESOURCE_USER = Core.RESOURCE_USER;

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSaveUserUsesHttpCreatedOnPersist() {
        CookieStore cookieStore = null;

        try {
            String endpoint = System.getenv("LDS_SERVER_ENDPOINT_EXTERNAL");
            String url = endpoint + "/login";

            cookieStore = new BasicCookieStore();
            CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

            HttpPost post = new HttpPost(url);

            // add header
            post.setHeader("User-Agent", "Mozilla/5.0");

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("user", "admin"));
            urlParameters.add(new BasicNameValuePair("password", "pass"));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            HttpResponse response = client.execute(post);
            List<Cookie> cookieList = cookieStore.getCookies();
            System.out.println("Response Code : "
                    + response.getStatusLine().getStatusCode());
        } catch (Exception e) {
            System.err.println(e);
        }

        try {
            String endpoint = System.getenv("LDS_SERVER_ENDPOINT_EXTERNAL");
            String url = endpoint + RESOURCE_USER;

            CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

            HttpPost post = new HttpPost(url);

            // add header
            post.setHeader("User-Agent", "Mozilla/5.0");
            post.setHeader("Content-type", "application/json");

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("user", "admin"));
            urlParameters.add(new BasicNameValuePair("password", "pass"));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            String json = null;
            User user = newUser();
            if (user != null) {
                try {
                    json = objectMapper.writeValueAsString(user);
                /*if (obj instanceof User) {
                    // Workaround to transfer password
                    json = json.substring(0, json.length() - 1);
                    json += ",\"password\":\"" + PASSWORD + "\"}";
                }*/
                } catch (JsonProcessingException e) {
                    // do nothing
                }
            }
            HttpEntity entity = new ByteArrayEntity(json.getBytes("UTF-8"));
            post.setEntity(entity);

            HttpResponse response = client.execute(post);
            System.out.println("Response Code : "
                    + response.getStatusLine().getStatusCode());
            Assert.assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(response.getStatusLine().getStatusCode()));
        } catch (Exception e) {
            System.err.println(e);
        }

        //HttpRequestWrapper requestWrapper = post().resource(RESOURCE_USER).body(newUser()).asAdmin();
        //ResponseEntity<User> response = requestWrapper.exec(User.class);
        //Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
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
