package de.hska.ld.oidc.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.core.exception.UserNotAuthorizedException;
import de.hska.ld.core.exception.ValidationException;
import de.hska.ld.oidc.dto.SSSAuthDto;
import de.hska.ld.oidc.dto.SSSLivingdocsRequestDto;
import de.hska.ld.oidc.dto.SSSLivingdocsResponseDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSSClient {
    @Autowired
    private Environment env;

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
        String url = "http://test-ll.know-center.tugraz.at/layers.test/auth/auth/";

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

    public SSSLivingdocsResponseDto createDocument(String accessToken) throws IOException {
        // TODO enable https
        String url = "http://test-ll.know-center.tugraz.at/layers.test/livingdocs/livingdocs/";

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
        sssLivingdocsRequestDto.setUri("https://localhost:9000/document/3");
        sssLivingdocsRequestDto.setDescription("description2");
        //sssLivingdocsRequestDto.setDiscussion();
        //sssLivingdocsRequestDto.setLabel();
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
}
