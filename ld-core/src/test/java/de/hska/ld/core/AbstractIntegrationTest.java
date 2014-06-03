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
import de.hska.ld.core.persistence.domain.User;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
@IntegrationTest
@WebAppConfiguration
public abstract class AbstractIntegrationTest {

    protected static final byte[] AUTH_USER = "user:pass".getBytes();
    protected static final byte[] AUTH_ADMIN = "admin:pass".getBytes();
    private static final String URL_API = "http://localhost:9000/api";

    protected Exception expectedException;
    protected HttpStatusCodeException expectedClientException;

    private ObjectMapper objectMapper = new ObjectMapper();
    private RestTemplate template = new RestTemplate();

    @After
    public void tearDown() throws Exception {
        expectedClientException = null;
    }

    protected <T> ResponseEntity<T> exchange(String resource, HttpMethod method, HttpEntity<?> requestEntity,
                                             Class<T> responseType) {
        return template.exchange(URL_API + resource, method, requestEntity, responseType);
    }

    protected <T> T getForObject(String resource, byte[] auth, Class<T> responseType) {
        return getForObject(resource, auth, responseType, null);
    }

    protected <T> T getForObject(String resource, Class<T> responseType) {
        return getForObject(resource, null, responseType, null);
    }

    protected <T> T getForObject(String resource, Class<T> responseType, Map<String, ?> urlVariables) {
        return getForObject(resource, null, responseType, urlVariables);
    }

    protected <T> T getForObject(String resource, final byte[] auth, Class<T> responseType, Map<String, ?> urlVariables) {
        resource = URL_API + resource;
        if (auth == null) {
            if (urlVariables == null) {
                return template.getForObject(resource, responseType);
            } else {
                return template.getForObject(resource, responseType, urlVariables);
            }
        } else {
            RestTemplate objectTemplate = new RestTemplate();
            ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {
                @Override
                public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                                    ClientHttpRequestExecution execution) throws IOException {
                    byte[] encodedAuthorisation = Base64.encode(auth);
                    request.getHeaders().add("Authorization", "Basic " + new String(encodedAuthorisation));
                    return execution.execute(request, body);
                }
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

    protected HttpEntity<String> createHeader() {
        return createHeader(null, null);
    }

    protected HttpEntity<String> createUserHeader() {
        return createHeader(null, AUTH_USER);
    }

    protected HttpEntity<String> createAdminHeader() {
        return createHeader(null, AUTH_ADMIN);
    }

    protected HttpEntity<String> createHeader(Object obj) {
        return createHeader(obj, null);
    }

    protected HttpEntity<String> createUserHeader(Object obj) {
        return createHeader(obj, AUTH_USER);
    }

    protected HttpEntity<String> createAdminHeader(Object obj) {
        return createHeader(obj, AUTH_ADMIN);
    }

    protected HttpEntity<String> createHeader(Object obj, byte[] auth) {
        String json = null;
        if (obj != null) {
            try {
                json = objectMapper.writeValueAsString(obj);
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

    protected void setAuthentication(User user) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user,
                user.getPassword()));
    }
}
