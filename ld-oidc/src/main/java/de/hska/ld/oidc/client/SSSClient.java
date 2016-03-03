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

package de.hska.ld.oidc.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.core.client.PostClientRequest;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.core.logging.ExceptionLogger;
import de.hska.ld.oidc.client.exception.AuthenticationNotValidException;
import de.hska.ld.oidc.dto.*;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

@Component("ld-oidc-sssclient")
public class SSSClient {

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Environment env;

    @Autowired
    private ExceptionLogger exceptionLogger;

    private int sssAPIVersion = -1;

    public int getSssAPIVersion() {
        if (sssAPIVersion == -1) {
            sssAPIVersion = Integer.parseInt(env.getProperty("sss.api.version"));
        }
        return sssAPIVersion;
    }

    public String getSssServerAddress() {
        return env.getProperty("sss.server.endpoint");
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
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/auth/auth/";
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/auth/";
        }

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

    public SSSLivingdocsResponseDto createDocument(Document document, String discussionId, String accessToken) throws IOException, AuthenticationNotValidException {
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/livingdocs/livingdocs/";
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/livingdocs/";
        }

        HttpClient client = getHttpClientFor(url);
        HttpPost post = new HttpPost(url);
        addHeaderInformation(post, accessToken);

        SSSLivingdocsRequestDto sssLivingdocsRequestDto = new SSSLivingdocsRequestDto();
        String externalServerAddress = env.getProperty("sss.document.name.prefix");
        sssLivingdocsRequestDto.setUri(externalServerAddress + document.getId());
        sssLivingdocsRequestDto.setDescription(document.getDescription());
        if (discussionId != null) {
            sssLivingdocsRequestDto.setDiscussion(discussionId);
        }
        sssLivingdocsRequestDto.setLabel(document.getTitle());
        String sssLivingdocsRequestDtoString = mapper.writeValueAsString(sssLivingdocsRequestDto);
        StringEntity stringEntity = new StringEntity(sssLivingdocsRequestDtoString, ContentType.create("application/json", "UTF-8"));
        post.setEntity(stringEntity);

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new AuthenticationNotValidException();
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
            return mapper.readValue(result.toString(), SSSLivingdocsResponseDto.class);
        } catch (Exception e) {
            return null;
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
    }

    public SSSDiscsDto getDiscussionsForDocument(Long documentId, String accessToken) throws IOException {
        //http://test-ll.know-center.tugraz.at/layers.test/discs/discs/targets/http%253A%252F%252F178.62.62.23%253A9000%252Fdocument%252F65554
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/discs/discs/filtered/targets/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8");
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/discs/filtered/targets/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8");
        }

        HttpClient client = getHttpClientFor(url);
        HttpPost post = new HttpPost(url);
        addHeaderInformation(post, accessToken);

        SSSDiscsFilteredTargetsRequestDto sssDiscsFilteredTargetsRequestDto = new SSSDiscsFilteredTargetsRequestDto();
        sssDiscsFilteredTargetsRequestDto.setSetAttachedEntities(true);
        sssDiscsFilteredTargetsRequestDto.setSetCircleTypes(true);
        sssDiscsFilteredTargetsRequestDto.setSetComments(true);
        sssDiscsFilteredTargetsRequestDto.setSetEntries(true);
        sssDiscsFilteredTargetsRequestDto.setSetLikes(true);
        sssDiscsFilteredTargetsRequestDto.setSetTags(true);
        String sssLivingdocsRequestDtoString = mapper.writeValueAsString(sssDiscsFilteredTargetsRequestDto);
        StringEntity stringEntity = new StringEntity(sssLivingdocsRequestDtoString, ContentType.create("application/json", "UTF-8"));
        post.setEntity(stringEntity);

        HttpResponse response = client.execute(post);
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
            return mapper.readValue(result.toString(), SSSDiscsDto.class);
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

    public SSSFileEntitiesDto getFileEntity(List<String> attachmentIds, String accessToken) throws IOException {
        String attachmentIdString = "";
        boolean firstAttachmentId = true;
        for (String attachmentId : attachmentIds) {
            String separator = ",";
            if (firstAttachmentId) {
                separator = "";
                firstAttachmentId = false;
            }
            try {
                attachmentIdString += separator + URLEncoder.encode(attachmentId, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        attachmentIdString = URLEncoder.encode(attachmentIdString, "UTF-8");
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/entities/entities/filtered/" + attachmentIdString;
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/entities/filtered/" + attachmentIdString;
        }

        HttpClient client = getHttpClientFor(url);
        HttpPost post = new HttpPost(url);
        addHeaderInformation(post, accessToken);

        SSSEntitiesFilteredEntitiesRequestDto sssDiscsFilteredTargetsRequestDto = new SSSEntitiesFilteredEntitiesRequestDto();
        sssDiscsFilteredTargetsRequestDto.setSetDiscs(false);
        sssDiscsFilteredTargetsRequestDto.setSetThumb(false);
        String sssLivingdocsRequestDtoString = mapper.writeValueAsString(sssDiscsFilteredTargetsRequestDto);
        StringEntity stringEntity = new StringEntity(sssLivingdocsRequestDtoString, ContentType.create("application/json", "UTF-8"));
        post.setEntity(stringEntity);

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        if (response.getStatusLine().getStatusCode() != 200) {
            if (response.getStatusLine().getStatusCode() == 403) {
                throw new UserNotAuthorizedException();
            }
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
            SSSFileEntitiesDto fileEntitiesDto = mapper.readValue(result.toString(), SSSFileEntitiesDto.class);

            return fileEntitiesDto;
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

    public SSSCreateDiscResponseDto createDiscussion(String documentId, SSSCreateDiscRequestDto discRequestDto, String accessToken, String episodeId) throws IOException {
        String sssDocumentId = env.getProperty("sss.document.name.prefix") + documentId;
        SSSCreateDiscRequestDto sssCreateDiscRequestDto = new SSSCreateDiscRequestDto();
        sssCreateDiscRequestDto.setDescription(discRequestDto.getDescription());
        sssCreateDiscRequestDto.setLabel(discRequestDto.getLabel());
        sssCreateDiscRequestDto.setEntities(discRequestDto.getEntities());
        discRequestDto.getTargets().add(sssDocumentId);
        if (episodeId != null) {
            discRequestDto.getTargets().add(episodeId);
        }
        sssCreateDiscRequestDto.setTargets(discRequestDto.getTargets());

        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/discs/discs";
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/discs";
        }

        HttpClient client = getHttpClientFor(url);
        HttpPost post = new HttpPost(url);
        addHeaderInformation(post, accessToken);

        String sssLivingdocsRequestDtoString = mapper.writeValueAsString(sssCreateDiscRequestDto);
        StringEntity stringEntity = new StringEntity(sssLivingdocsRequestDtoString, ContentType.create("application/json", "UTF-8"));
        post.setEntity(stringEntity);
        BufferedReader rd = null;

        HttpResponse response = client.execute(post);
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
            SSSCreateDiscResponseDto sssCreateDiscResponseDto = mapper.readValue(result.toString(), SSSCreateDiscResponseDto.class);
            return sssCreateDiscResponseDto;
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

    public void addTagTo(String discOrEntry, String tag, String accessToken) throws IOException {
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/tags/tags";
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/tags";
        }

        SSSTagRequestDto sssTagRequestDto = new SSSTagRequestDto();
        sssTagRequestDto.setEntity(discOrEntry);
        sssTagRequestDto.setLabel(tag);

        HttpClient client = getHttpClientFor(url);
        HttpPost post = new HttpPost(url);
        addHeaderInformation(post, accessToken);

        String sssLivingdocsRequestDtoString = mapper.writeValueAsString(sssTagRequestDto);
        StringEntity stringEntity = new StringEntity(sssLivingdocsRequestDtoString, ContentType.create("application/json", "UTF-8"));
        post.setEntity(stringEntity);
        BufferedReader rd = null;

        HttpResponse response = client.execute(post);
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

    public SSSEntryForDiscussionResponseDto createEntryForDiscussion(SSSEntryForDiscussionRequestDto entryForDiscRequestDto, String accessToken) throws IOException {
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/discs/discs";
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/discs";
        }

        HttpClient client = getHttpClientFor(url);
        HttpPost post = new HttpPost(url);
        addHeaderInformation(post, accessToken);

        String sssLivingdocsRequestDtoString = mapper.writeValueAsString(entryForDiscRequestDto);
        StringEntity stringEntity = new StringEntity(sssLivingdocsRequestDtoString, ContentType.create("application/json", "UTF-8"));
        post.setEntity(stringEntity);
        BufferedReader rd = null;

        HttpResponse response = client.execute(post);
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
            SSSEntryForDiscussionResponseDto sssEntryForDiscussionResponseDto = mapper.readValue(result.toString(), SSSEntryForDiscussionResponseDto.class);
            return sssEntryForDiscussionResponseDto;
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

    public SSSLivingDocResponseDto getLDocByIdWithEntities(Long documentId, String accessToken) throws IOException, AuthenticationNotValidException {
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/livingdocs/livingdocs/filtered/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8");
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/livingdocs/filtered/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8");
        }

        PostClientRequest postClientRequest = new PostClientRequest(url, "getLDocById");
        StringEntity stringEntity = new StringEntity("{\"setAttachedEntities\": true}", ContentType.create("application/json", "UTF-8"));
        try {
            postClientRequest.execute(stringEntity, accessToken);
        } catch (Exception e) {
            exceptionLogger.log("getLDocByIdWithEntities Statuscode", e);
        }
        SSSLivingDocResponseDto sssLivingDocResponseDto = postClientRequest.getParsedBody(SSSLivingDocResponseDto.class);
        return sssLivingDocResponseDto;
    }

    public SSSLivingDocResponseDto getLDocById(Long documentId, String accessToken) throws IOException, AuthenticationNotValidException {
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/livingdocs/livingdocs/filtered/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8");
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/livingdocs/filtered/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8");
        }

        PostClientRequest postClientRequest = new PostClientRequest(url, "getLDocById");
        StringEntity stringEntity = new StringEntity("{}", ContentType.create("application/json", "UTF-8"));
        try {
            postClientRequest.execute(stringEntity, accessToken);
        } catch (Exception e) {
            exceptionLogger.log("getLDocById Statuscode", e);
        }
        SSSLivingDocResponseDto sssLivingDocResponseDto = postClientRequest.getParsedBody(SSSLivingDocResponseDto.class);
        return sssLivingDocResponseDto;
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

    public SSSLivingDocResponseDto getLDocEmailsById(Long documentId, String accessToken) throws IOException, AuthenticationNotValidException {
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/livingdocs/livingdocs/filtered/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8");
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/livingdocs/filtered/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8");
        }


        StringEntity stringEntity = new StringEntity("{\"setUsers\": true}", ContentType.create("application/json", "UTF-8"));
        PostClientRequest postClientRequest = new PostClientRequest(url, "getLDocEmailsById1");
        try {
            postClientRequest.execute(stringEntity, accessToken);
        } catch (Exception e) {
            exceptionLogger.log("getLDocEmailsById1 Statuscode", e);
        }
        SSSLivingDocResponseDto sssLivingDocResponseDto = postClientRequest.getParsedBody(SSSLivingDocResponseDto.class);
        if (sssLivingDocResponseDto == null) {
            // TODO HACK this is a workaround because the configuration in the production system was set wrong and the
            // ids can't be changed in a quick way
            String url2 = null;
            if (getSssAPIVersion() == 1) {
                url2 = env.getProperty("sss.server.endpoint") + "/livingdocs/livingdocs/filtered/" + URLEncoder.encode(URLEncoder.encode("http://178.62.62.23:9000/" + documentId, "UTF-8"), "UTF-8");
            } else {
                url2 = env.getProperty("sss.server.endpoint") + "/rest/livingdocs/filtered/" + URLEncoder.encode(URLEncoder.encode("http://178.62.62.23:9000/" + documentId, "UTF-8"), "UTF-8");
            }

            PostClientRequest postClientRequest2 = new PostClientRequest(url2, "getLDocEmailsById2");
            StringEntity stringEntity2 = new StringEntity("{}", ContentType.create("application/json", "UTF-8"));
            try {
                postClientRequest2.execute(stringEntity2, accessToken);
            } catch (Exception e) {
                exceptionLogger.log("getLDocEmailsById2 Statuscode", e);
            }
            return postClientRequest2.getParsedBody(SSSLivingDocResponseDto.class);
        } else {
            return sssLivingDocResponseDto;
        }
    }

    private HttpPost addHeaderInformation(HttpPost post, String accessToken) {
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);
        return post;
    }

    private HttpPut addHeaderInformation(HttpPut put, String accessToken) {
        put.setHeader("Content-type", "application/json");
        put.setHeader("User-Agent", "Mozilla/5.0");
        put.setHeader("Authorization", "Bearer " + accessToken);
        return put;
    }

    private HttpGet addHeaderInformation(HttpGet get, String accessToken) {
        get.setHeader("Content-type", "application/json");
        get.setHeader("User-Agent", "Mozilla/5.0");
        get.setHeader("Authorization", "Bearer " + accessToken);
        return get;
    }

    public void shareLDocWith(Long documentId, List<String> sssUserIdsTheLDocIsNotSharedWith, String accessToken) throws IOException {
        String url = null;
        if (getSssAPIVersion() == 1) {
            url = env.getProperty("sss.server.endpoint") + "/entities/entities/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8") + "/share";
        } else {
            url = env.getProperty("sss.server.endpoint") + "/rest/entities/" + URLEncoder.encode(URLEncoder.encode(env.getProperty("sss.document.name.prefix") + documentId, "UTF-8"), "UTF-8") + "/share";
        }

        HttpClient client = getHttpClientFor(url);
        HttpPut put = new HttpPut(url);
        addHeaderInformation(put, accessToken);

        SSSShareEntityDto sssShareEntityDto = new SSSShareEntityDto();
        sssShareEntityDto.setUsers(sssUserIdsTheLDocIsNotSharedWith);

        String requestDtoString = mapper.writeValueAsString(sssShareEntityDto);
        StringEntity stringEntity = new StringEntity(requestDtoString, ContentType.create("application/json", "UTF-8"));
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
        } catch (ValidationException ve) {
            throw ve;
        } catch (Exception e) {
            e.printStackTrace();
            //return null;
        } finally {
            if (rd != null) {
                rd.close();
            }
        }
    }

    public SSSCircleInfoWrapper getCircleInformation(String episodeId, String accessToken) throws IOException {
        String url = env.getProperty("sss.server.endpoint")
                + "/rest/learneps/"
                + URLEncoder.encode(URLEncoder.encode(episodeId, "UTF-8"), "UTF-8")
                + "/structure/circles/entities";

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
            return mapper.readValue(result.toString(), SSSCircleInfoWrapper.class);
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
}
