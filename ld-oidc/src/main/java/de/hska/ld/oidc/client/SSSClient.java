package de.hska.ld.oidc.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.oidc.dto.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.annotation.PostConstruct;
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

public class SSSClient {

    private String documentNamePrefix = "http://178.62.62.23:9000/document/";
    private String sssServerAddress = "http://test-ll.know-center.tugraz.at/layers.test";

    public String getSssServerAddress() {
        return sssServerAddress;
    }

    @PostConstruct
    public void postConstruct() {
        // TODO set env variables if needed
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
        // TODO enable https
        String url = sssServerAddress + "/auth/auth/";

        //HttpClient client = HttpClientBuilder.create().build();
        //HttpClient client = createHttpsClient();
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);

        // add header
        get.setHeader("Content-type", "application/json");
        get.setHeader("User-Agent", "Mozilla/5.0");
        get.setHeader("Authorization", "Bearer " + accessToken);

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
            ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

    public void getAllLDocs(String accessToken) {

    }

    public SSSLivingdocsResponseDto createDocument(Document document, String discussionId, String accessToken) throws IOException {
        // TODO enable https
        String url = sssServerAddress + "/livingdocs/livingdocs/";

        //HttpClient client = HttpClientBuilder.create().build();
        //HttpClient client = createHttpsClient();
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SSSLivingdocsRequestDto sssLivingdocsRequestDto = new SSSLivingdocsRequestDto();
        String externalServerAddress = documentNamePrefix; //env.getProperty("module.core.oidc.server.endpoint.external.url");
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
            mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(result.toString(), SSSLivingdocsResponseDto.class);
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

    public SSSDiscsDto getDiscussionsForDocument(Long documentId, String accessToken) throws IOException {
        // TODO enable https
        //http://test-ll.know-center.tugraz.at/layers.test/discs/discs/targets/http%253A%252F%252F178.62.62.23%253A9000%252Fdocument%252F65554
        String url = sssServerAddress + "/discs/discs/filtered/targets/" + URLEncoder.encode(URLEncoder.encode(documentNamePrefix + documentId, "UTF-8"), "UTF-8");

        //HttpClient client = HttpClientBuilder.create().build();
        //HttpClient client = createHttpsClient();
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
            mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
        String url = sssServerAddress + "/entities/entities/filtered/" + attachmentIdString;

        //HttpClient client = HttpClientBuilder.create().build();
        //HttpClient client = createHttpsClient();
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
            mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

    public SSSCreateDiscResponseDto createDiscussion(String documentId, SSSCreateDiscRequestDto discRequestDto, String accessToken) throws IOException {
        String sssDocumentId = documentNamePrefix + documentId;
        SSSCreateDiscRequestDto sssCreateDiscRequestDto = new SSSCreateDiscRequestDto();
        sssCreateDiscRequestDto.setDescription(discRequestDto.getDescription());
        sssCreateDiscRequestDto.setLabel(discRequestDto.getLabel());
        sssCreateDiscRequestDto.setEntities(discRequestDto.getEntities());
        discRequestDto.getTargets().add(sssDocumentId);
        sssCreateDiscRequestDto.setTargets(discRequestDto.getTargets());

        String url = sssServerAddress + "/discs/discs";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
            mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
        String url = sssServerAddress + "/tags/tags";

        SSSTagRequestDto sssTagRequestDto = new SSSTagRequestDto();
        sssTagRequestDto.setEntity(discOrEntry);
        sssTagRequestDto.setLabel(tag);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
        String url = sssServerAddress + "/discs/discs";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("Content-type", "application/json");
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Authorization", "Bearer " + accessToken);

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
}
