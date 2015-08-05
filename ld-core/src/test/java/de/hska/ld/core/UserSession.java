package de.hska.ld.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.core.persistence.domain.User;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.junit.Assert;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static de.hska.ld.core.util.CoreUtil.PASSWORD;

public class UserSession {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "pass";
    private static final String USER_USERNAME = "user";
    private static final String USER_PASSWORD = "pass";

    private String endpoint = System.getenv("LDS_SERVER_ENDPOINT_EXTERNAL");
    private String loginURL = endpoint + "/login";

    private CookieStore cookieStore = new BasicCookieStore();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserSession() {

    }

    public UserSession loginAsAdmin() throws Exception {
        return login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public static UserSession admin() throws Exception {
        UserSession userSession = new UserSession();
        try {
            userSession.loginAsAdmin();
        } catch(Exception e) {
            Assert.fail();
        }
        return userSession;
    }

    public static UserSession user() throws Exception {
        UserSession userSession = new UserSession();
        try {
            userSession.loginAsUser();
        } catch(Exception e) {
            Assert.fail();
        }
        return userSession;
    }

    public UserSession loginAsUser() throws Exception {
        return login(USER_USERNAME, USER_PASSWORD);
    }

    private CloseableHttpClient getClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
        CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).setSSLSocketFactory(
                sslsf).build();
        return client;
    }

    public UserSession login(String username, String password) throws Exception {
        CloseableHttpClient client = getClient();

        HttpPost post = new HttpPost(loginURL);
        post.setHeader("User-Agent", "Mozilla/5.0");
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("user", username));
        urlParameters.add(new BasicNameValuePair("password", password));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception("Could not establish logged in connection!");
        }
        return this;
    }

    public static HttpStatus getStatusCode(HttpResponse response){
        return HttpStatus.valueOf(response.getStatusLine().getStatusCode());
    }

    public static <T> T getBody(HttpResponse response, Class<T> clazz) throws IOException {
        HttpEntity entity = response.getEntity();
        String body = IOUtils.toString(entity.getContent(), Charset.forName("UTF-8"));
        ObjectMapper objectMapper = new ObjectMapper();
        T obj = objectMapper.readValue(body, clazz);
        return obj;
    }

    public <T> HttpResponse post(String url, T body) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CloseableHttpClient client = getClient();
        HttpPost post = new HttpPost(endpoint + url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");
        post.setHeader("Content-type", "application/json");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("user", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "pass"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        String json = null;
        if (body != null) {
            try {
                json = objectMapper.writeValueAsString(body);
                if (body instanceof User) {
                    // Workaround to transfer password
                    json = json.substring(0, json.length() - 1);
                    json += ",\"password\":\"" + PASSWORD + "\"}";
                }
            } catch (JsonProcessingException e) {
                // do nothing
            }
        }
        HttpEntity entity = null;
        if (json != null) {
            entity = new ByteArrayEntity(json.getBytes("UTF-8"));
        }
        post.setEntity(entity);

        return client.execute(post);
    }

    public <T> HttpResponse put(String url, T body) throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        CloseableHttpClient client = getClient();
        HttpPut put = new HttpPut(endpoint + url);

        // add header
        put.setHeader("User-Agent", "Mozilla/5.0");
        put.setHeader("Content-type", "application/json");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("user", "admin"));
        urlParameters.add(new BasicNameValuePair("password", "pass"));

        put.setEntity(new UrlEncodedFormEntity(urlParameters));
        String json = null;
        if (body != null) {
            try {
                json = objectMapper.writeValueAsString(body);
                if (body instanceof User) {
                    // Workaround to transfer password
                    json = json.substring(0, json.length() - 1);
                    json += ",\"password\":\"" + PASSWORD + "\"}";
                }
            } catch (JsonProcessingException e) {
                // do nothing
            }
        }
        HttpEntity entity = null;
        if (json != null) {
            entity = new ByteArrayEntity(json.getBytes("UTF-8"));
        }
        put.setEntity(entity);

        return client.execute(put);
    }

    public <T> HttpResponse delete(String url, List<NameValuePair> urlParameters) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        URIBuilder builder = new URIBuilder(endpoint + url);
        if (urlParameters != null) {
            urlParameters.forEach(param -> {
                builder.setParameter(param.getName(), param.getValue());
            });
        }
        URI uri = builder.build();

        CloseableHttpClient client = getClient();
        HttpDelete delete = new HttpDelete(uri);
        delete.setHeader("User-Agent", "Mozilla/5.0");
        delete.setHeader("Content-type", "application/json");
        return client.execute(delete);
    }

    public HttpResponse get(String url, List<NameValuePair> urlParameters) throws IOException, URISyntaxException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        URIBuilder builder = new URIBuilder(endpoint + url);
        if (urlParameters != null) {
            urlParameters.forEach(param -> {
                builder.setParameter(param.getName(), param.getValue());
            });
        }
        URI uri = builder.build();

        CloseableHttpClient client = getClient();
        HttpGet get = new HttpGet(uri);
        get.setHeader("User-Agent", "Mozilla/5.0");
        get.setHeader("Content-type", "application/json");
        return client.execute(get);
    }
}
