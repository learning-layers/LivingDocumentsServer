package de.hska.ld.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.core.persistence.domain.User;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static de.hska.ld.core.fixture.CoreFixture.PASSWORD;

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

    public UserSession loginAsUser() throws Exception {
        return login(USER_USERNAME, USER_PASSWORD);
    }

    public UserSession login(String username, String password) throws Exception {
        CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

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

    public <T> HttpResponse postJson(String url, T body) throws IOException {
        CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
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

    public <T> HttpResponse delete(String url, List<NameValuePair> urlParameters) throws IOException, URISyntaxException {
        URIBuilder builder = new URIBuilder(endpoint + url);
        if (urlParameters != null) {
            urlParameters.forEach(param -> {
                builder.setParameter(param.getName(), param.getValue());
            });
        }
        URI uri = builder.build();

        CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        HttpDelete delete = new HttpDelete(uri);
        delete.setHeader("User-Agent", "Mozilla/5.0");
        delete.setHeader("Content-type", "application/json");
        return client.execute(delete);
    }
}
