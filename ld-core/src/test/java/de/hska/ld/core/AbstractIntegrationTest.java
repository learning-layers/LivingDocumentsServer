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

package de.hska.ld.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.core.exception.ApplicationError;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static de.hska.ld.core.fixture.CoreFixture.PASSWORD;
import static de.hska.ld.core.fixture.CoreFixture.newUser;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@IntegrationTest
@WebAppConfiguration
public abstract class AbstractIntegrationTest {

    private static final String BASE_URL = "http://localhost:9000";
    private static final byte[] AUTH_USER = ("user" + ":pass").getBytes();
    private static final byte[] AUTH_ADMIN = ("admin" + ":pass").getBytes();

    @Autowired
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate template = new RestTemplate();

    protected HttpStatusCodeException expectedClientException;
    protected ApplicationError applicationError;

    protected User testUser;

    @Before
    public void setUp() throws Exception {
        testUser = userService.save(newUser());
    }

    @After
    public void tearDown() throws Exception {
        expectedClientException = null;
    }

    protected void parseApplicationError(String body) {
        try {
            applicationError = objectMapper.readValue(body, ApplicationError.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HttpEntity<String> createHeaderAndBody(Object obj, byte[] auth) {
        String json = null;
        if (obj != null) {
            try {
                json = objectMapper.writeValueAsString(obj);
                if (obj instanceof User) {
                    // Workaround to transfer password
                    json = json.substring(0, json.length() - 1);
                    json += ",\"password\":\"" + PASSWORD + "\"}";
                }
            } catch (JsonProcessingException e) {
                // do nothing
            }
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        if (auth != null) {
            byte[] encodedAuthorisation = Base64.encode(auth);
            headers.add("Authorization", "Basic " + new String(encodedAuthorisation));
        }

        if (json == null) {
            return new HttpEntity<>(headers);
        } else {
            return new HttpEntity<>(json, headers);
        }
    }

    public Map getPage(String resource, User user) {
        return getForObject(resource, user, Map.class, null);
    }

    public Map getPage(String resource, User user, Map<String, ?> urlVariables) {
        return getForObject(resource, user, Map.class, urlVariables);
    }

    @SuppressWarnings("unchecked")
    protected <T> T getForObject(String resource, User user, Class<T> responseType, Map<String, ?> urlVariables) {
        resource = BASE_URL + resource;
        if (user == null) {
            if (urlVariables == null) {
                return template.getForObject(resource, responseType);
            } else {
                return template.getForObject(resource, responseType, urlVariables);
            }
        } else {
            String usernameAndPassword = user.getUsername() + ":" + PASSWORD;
            byte[] auth = usernameAndPassword.getBytes();
            RestTemplate objectTemplate = new RestTemplate();
            ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
                byte[] encodedAuthorisation = Base64.encode(auth);
                request.getHeaders().add("Authorization", "Basic " + new String(encodedAuthorisation));
                return execution.execute(request, body);
            };
            List<ClientHttpRequestInterceptor> list = new ArrayList<>();
            list.add(interceptor);
            objectTemplate.setInterceptors(list);
            if (urlVariables == null) {
                return objectTemplate.getForObject(resource, responseType);
            } else {
                return objectTemplate.getForObject(resource, responseType, urlVariables);
            }
        }
    }

    protected void setAuthentication(User user) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,
                user.getPassword(), user.getAuthorities()));
    }

    protected HttpRequestWrapper get() {
        return new HttpRequestWrapper(HttpMethod.GET);
    }

    protected HttpRequestWrapper post() {
        return new HttpRequestWrapper(HttpMethod.POST);
    }

    protected HttpRequestWrapper put() {
        return new HttpRequestWrapper(HttpMethod.PUT);
    }

    protected HttpRequestWrapper delete() {
        return new HttpRequestWrapper(HttpMethod.DELETE);
    }

    public class HttpRequestWrapper {

        private String resource;
        private HttpMethod httpMethod;
        private Object body;
        private byte[] auth;

        public HttpRequestWrapper(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
        }

        public ResponseEntity<Void> exec() {
            return exec(Void.class);
        }

        public <T> ResponseEntity<T> exec(Class<T> responseType) {
            return template.exchange(BASE_URL + resource, httpMethod, createHeaderAndBody(body, auth), responseType);
        }

        public HttpRequestWrapper resource(String resource) {
            this.resource = resource;
            return this;
        }

        public HttpRequestWrapper body(Object body) {
            this.body = body;
            return this;
        }

        public HttpRequestWrapper as(byte[] auth) {
            this.auth = auth;
            return this;
        }

        public HttpRequestWrapper as(User user) {
            String usernameAndPassword = user.getUsername() + ":" + PASSWORD;
            this.auth = usernameAndPassword.getBytes();
            return this;
        }

        public HttpRequestWrapper asUser() {
            this.auth = AUTH_USER;
            return this;
        }

        public HttpRequestWrapper asAdmin() {
            this.auth = AUTH_ADMIN;
            return this;
        }
    }
}