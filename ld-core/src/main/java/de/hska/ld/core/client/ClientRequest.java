/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2015, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.hska.ld.core.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.core.config.ApplicationContextProvider;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.logging.ExceptionLogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.HttpStatus;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ClientRequest<T> {
    protected ExceptionLogger exceptionLogger;
    protected HttpClient client;
    protected HttpResponse response;
    protected String url;
    protected String action;
    protected T parsedBody;
    private ObjectMapper mapper;

    public ClientRequest(String url, String action) {
        this.exceptionLogger = ApplicationContextProvider.getApplicationContextStatic().getBean("exceptionLogger", ExceptionLogger.class);
        this.mapper = ApplicationContextProvider.getApplicationContextStatic().getBean("objectMapper", ObjectMapper.class);
        this.url = url;
        this.client = getHttpClientFor(url);
        this.action = action;
    }

    public HttpStatus getResponseStatusCode() {
        return HttpStatus.valueOf(this.response.getStatusLine().getStatusCode());
    }

    protected void processResponse() {
        if (this.getResponseStatusCode() != HttpStatus.OK) {
            this.exceptionLogger.log(this.action, "HTTPStatus=" + this.getResponseStatusCode().toString());
        }
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            if (result.toString().contains("\"error_description\":\"Invalid access token:")) {
                throw new ValidationException("access token is invalid");
            }
            TypeReference typeReference = new TypeReference<T>() {
            };
            this.parsedBody = mapper.readValue(result.toString(), typeReference);
        } catch (IOException e) {
            this.exceptionLogger.log(this.action, e);
        } finally {
            if (rd != null) {
                try {
                    rd.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    public <T> T getParsedBody() {
        return (T) this.parsedBody;
    }

    private HttpClient getHttpClientFor(String url) {
        if (url.startsWith("https")) {
            try {
                return createHttpsClient();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Couldn't create https client!");
            }
        } else if (url.startsWith("http")) {
            return HttpClientBuilder.create().build();
        } else {
            throw new RuntimeException("Protocol unknown!");
        }
    }

    private CloseableHttpClient createHttpsClient() throws IOException {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, new TrustStrategy() {

                        @Override
                        public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                            return true;
                        }
                    })
                    .useProtocol("TLSv1.2")
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return HttpClients.custom()
                .setSSLContext(sslContext)
                .build();
    }
}
