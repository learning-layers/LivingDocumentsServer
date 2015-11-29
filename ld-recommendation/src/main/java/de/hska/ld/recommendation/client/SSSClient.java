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

package de.hska.ld.recommendation.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.persistence.domain.Tag;
import de.hska.ld.content.service.DocumentService;
import de.hska.ld.content.service.TagService;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.util.Core;
import de.hska.ld.recommendation.dto.RecommUpdateDto;
import de.hska.ld.recommendation.dto.RecommUpdateResponseDto;
import de.hska.ld.recommendation.dto.SSSAuthDto;
import de.hska.ld.recommendation.persistence.domain.DocumentRecommInfo;
import de.hska.ld.recommendation.service.DocumentRecommInfoService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Component("ld-recommendation-sssclient")
public class SSSClient {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRecommInfoService documentRecommInfoService;

    @Autowired
    private Environment env;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TagService tagService;

    public void performInitialSSSTagLoad(Long documentId, String accessToken) throws IOException {
        String url = env.getProperty("sss.server.endpoint") + "/recomm/recomm/update";
        Document document = documentService.findById(documentId);
        List<String> tagStringList = new ArrayList<String>();
        document.getTagList().forEach(t -> {
            tagStringList.add(t.getName());
        });
        if (tagStringList.size() > 0) {
            String userPrefix = env.getProperty("sss.user.name.prefix");
            String documentPrefix = env.getProperty("sss.document.name.prefix");

            RecommUpdateDto recommUpdateDto = new RecommUpdateDto();
            recommUpdateDto.setRealm("dieter1");
            recommUpdateDto.setForUser(userPrefix + Core.currentUser().getId());
            recommUpdateDto.setEntity(documentPrefix + document.getId());
            recommUpdateDto.setTags(tagStringList);

            HttpClient client = getHttpClientFor(url);
            HttpPut put = new HttpPut(url);
            addHeaderInformation(put, accessToken);

            String recommUpdateDtoString = mapper.writeValueAsString(recommUpdateDto);
            StringEntity stringEntity = new StringEntity(recommUpdateDtoString, ContentType.create("application/json", "UTF-8"));
            put.setEntity(stringEntity);
            BufferedReader rd = null;

            HttpResponse response = client.execute(put);
            System.out.println("Response Code : "
                    + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() != 200) {
                if (response.getStatusLine().getStatusCode() == 403) {
                    throw new UserNotAuthorizedException();
                }
            }

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
                mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                RecommUpdateResponseDto recommUpdateResponseDto = mapper.readValue(result.toString(), RecommUpdateResponseDto.class);
                if (!recommUpdateResponseDto.isWorked()) {
                    throw new Exception("Updating recommendations didn't work!");
                } else {
                    documentRecommInfoService.addDocumentRecommInfo(documentId);
                }
                return;
            } catch (ValidationException ve) {
                throw ve;
            } catch (Exception e) {
                return;
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }
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

    public SSSAuthDto authenticate(String accessToken) throws IOException {
        String url = env.getProperty("sss.server.endpoint") + "/auth/auth/";

        HttpClient client = getHttpClientFor(url);
        HttpGet get = new HttpGet(url);
        addHeaderInformation(get, accessToken);


        HttpResponse response = client.execute(get);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new UserNotAuthorizedException();
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
            return mapper.readValue(result.toString(), SSSAuthDto.class);
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            return null;
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
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

    private HttpPut addHeaderInformation(HttpPut put, String accessToken) {
        put.setHeader("Content-type", "application/json");
        put.setHeader("User-Agent", "Mozilla/5.0");
        put.setHeader("Authorization", "Bearer " + accessToken);
        return put;
    }

    private HttpPost addHeaderInformation(HttpPost post, String accessToken) {
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);
        return post;
    }

    private HttpGet addHeaderInformation(HttpGet get, String accessToken) {
        get.setHeader("Content-type", "application/json");
        get.setHeader("User-Agent", "Mozilla/5.0");
        get.setHeader("Authorization", "Bearer " + accessToken);
        return get;
    }

    public void addTagToDocument(Long documentId, Long tagId, String accessToken) throws IOException {
        String url = env.getProperty("sss.server.endpoint") + "/recomm/recomm/update";
        Document document = documentService.findById(documentId);
        List<String> tagStringList = new ArrayList<String>();

        Tag tag = tagService.findById(tagId);
        if (tag != null) {
            tagStringList.add(tag.getName());
            String userPrefix = env.getProperty("sss.user.name.prefix");
            String documentPrefix = env.getProperty("sss.document.name.prefix");

            RecommUpdateDto recommUpdateDto = new RecommUpdateDto();
            recommUpdateDto.setRealm("dieter1");
            recommUpdateDto.setForUser(userPrefix + Core.currentUser().getId());
            recommUpdateDto.setEntity(documentPrefix + document.getId());
            recommUpdateDto.setTags(tagStringList);

            HttpClient client = getHttpClientFor(url);
            HttpPut put = new HttpPut(url);
            addHeaderInformation(put, accessToken);

            String recommUpdateDtoString = mapper.writeValueAsString(recommUpdateDto);
            StringEntity stringEntity = new StringEntity(recommUpdateDtoString, ContentType.create("application/json", "UTF-8"));
            put.setEntity(stringEntity);
            BufferedReader rd = null;

            HttpResponse response = client.execute(put);
            System.out.println("Response Code : "
                    + response.getStatusLine().getStatusCode());

            if (response.getStatusLine().getStatusCode() != 200) {
                if (response.getStatusLine().getStatusCode() == 403) {
                    throw new UserNotAuthorizedException();
                }
            }

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
                mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                RecommUpdateResponseDto recommUpdateResponseDto = mapper.readValue(result.toString(), RecommUpdateResponseDto.class);
                if (!recommUpdateResponseDto.isWorked()) {
                    throw new Exception("Updating recommendations didn't work!");
                } else {
                    EntityTransaction tx = entityManager.getTransaction();
                    document = documentService.findById(document.getId());
                    DocumentRecommInfo documentRecommInfo = new DocumentRecommInfo();
                    documentRecommInfo.setDocument(document);
                    documentRecommInfo.setInitialImportToSSSDone(true);
                    try {
                        tx.begin();
                        entityManager.persist(documentRecommInfo);
                        entityManager.flush();
                        tx.commit();
                    } catch (Exception ex) {
                        tx.rollback();
                        throw ex;
                    }
                }
                return;
            } catch (ValidationException ve) {
                throw ve;
            } catch (Exception e) {
                return;
            } finally {
                if (rd != null) {
                    rd.close();
                }
            }
        }
    }

    public void retrieveRecommendations(Long documentId, String accessToken) throws IOException {
        String documentPrefix = env.getProperty("sss.document.name.prefix");
        String documentUrl = documentPrefix + documentId;
        String utf8DocumentUrl = URLEncoder.encode(URLEncoder.encode(documentUrl, "UTF-8"), "UTF-8");
        String url = env.getProperty("sss.server.endpoint") + "/recomm/recomm/recommUsersIgnoreAccessRights/realm/dieter1/entity/" + utf8DocumentUrl;
        HttpClient client = getHttpClientFor(url);
        HttpGet get = new HttpGet(url);
        addHeaderInformation(get, accessToken);

        BufferedReader rd = null;

        HttpResponse response = client.execute(get);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        if (response.getStatusLine().getStatusCode() != 200) {
            if (response.getStatusLine().getStatusCode() == 403) {
                throw new UserNotAuthorizedException();
            }
        }

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
            mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            RecommUpdateResponseDto recommUpdateResponseDto = mapper.readValue(result.toString(), RecommUpdateResponseDto.class);
            return;
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            return;
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
    }
}
