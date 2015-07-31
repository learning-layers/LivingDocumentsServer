package de.hska.ld.etherpad.client;

import de.hska.ld.etherpad.dto.EtherpadAuthorDto;
import de.hska.ld.etherpad.dto.EtherpadGroupDto;
import de.hska.ld.etherpad.dto.EtherpadGroupPadDto;
import de.hska.ld.etherpad.dto.EtherpadSessionDto;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.codehaus.jackson.map.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;

public class EtherpadClient {

    @Autowired
    private Environment env;

    private String etherpadEndpoint = null;
    private String etherpadAPIKey = null;

    @PostConstruct
    public void postConstruct() {
        etherpadEndpoint = System.getenv("LDS_ETHERPAD_ENDPOINT"); //env.getProperty("module.etherpad.endpoint");
        etherpadAPIKey = System.getenv("LDS_API_KEY"); //env.getProperty("module.etherpad.apikey");
    }

    public boolean checkIfSessionStillValid(Long currentTime, String sessionId, String groupID) throws IOException {
        String endpoint = etherpadEndpoint;
        String url = endpoint + "/api/1/getSessionInfo";

        //HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = createHttpsClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", etherpadAPIKey));
        urlParameters.add(new BasicNameValuePair("sessionID", sessionId));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        ObjectMapper mapper = new ObjectMapper();
        EtherpadSessionDto etherpadSessionDto = mapper.readValue(result.toString(), EtherpadSessionDto.class);
        if (etherpadSessionDto.getCode() == 1) {
            return false;
        } else {
            if (etherpadSessionDto.getData().getGroupID().equals(groupID) && etherpadSessionDto.getData().getValidUntil() - currentTime >= 10800) {
                return true;
            } else
                return false;
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

    public String createSession(String groupID, String authorID, Long validUntil) throws IOException {
        String sessionId = "";
        String endpoint = etherpadEndpoint;
        String url = endpoint + "/api/1/createSession";

        //HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = createHttpsClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", etherpadAPIKey));
        urlParameters.add(new BasicNameValuePair("groupID", groupID));
        urlParameters.add(new BasicNameValuePair("authorID", authorID));
        urlParameters.add(new BasicNameValuePair("validUntil", String.valueOf(validUntil)));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        ObjectMapper mapper = new ObjectMapper();
        EtherpadSessionDto etherpadSessionDto = mapper.readValue(result.toString(), EtherpadSessionDto.class);
        return etherpadSessionDto.getData().getSessionID();
    }

    public String getReadOnlyID(String groupPadId) throws IOException {
        String endpoint = etherpadEndpoint;
        String url = endpoint + "/api/1/getReadOnlyID";

        //HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = createHttpsClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", etherpadAPIKey));
        urlParameters.add(new BasicNameValuePair("padID", groupPadId));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        //{code: 0, message:"ok", data: {readOnlyID: "r.s8oes9dhwrvt0zif"}}
        //{code: 1, message:"padID does not exist", data: null}
        ObjectMapper mapper = new ObjectMapper();
        EtherpadSessionDto etherpadSessionDto = mapper.readValue(result.toString(), EtherpadSessionDto.class);
        if (etherpadSessionDto.getCode() != 1) {
            return etherpadSessionDto.getData().getReadOnlyID();
        } else {
            return null;
        }
    }

    public String createAuthor(String authorName) throws IOException {
        String sessionId = "";
        String endpoint = etherpadEndpoint;
        String url = endpoint + "/api/1/createAuthor";

        //HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = createHttpsClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", etherpadAPIKey));
        urlParameters.add(new BasicNameValuePair("name", authorName));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        ObjectMapper mapper = new ObjectMapper();
        EtherpadAuthorDto etherpadAuthorDto = mapper.readValue(result.toString(), EtherpadAuthorDto.class);
        return etherpadAuthorDto.getData().getAuthorID();
    }

    public String createGroupPad(String groupId, String padName) throws IOException {
        return createGroupPad(groupId, padName, null);
    }

    public String createGroupPad(String groupId, String padName, String discussionContent) throws IOException {
        String sessionId = "";
        String endpoint = etherpadEndpoint;
        String url = endpoint + "/api/1/createGroupPad";

        //HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = createHttpsClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", etherpadAPIKey));
        urlParameters.add(new BasicNameValuePair("groupID", groupId));
        urlParameters.add(new BasicNameValuePair("padName", padName));
        if (discussionContent != null) {
            urlParameters.add(new BasicNameValuePair("text", discussionContent));
        }

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        ObjectMapper mapper = new ObjectMapper();
        EtherpadGroupPadDto etherpadGroupPadDto = mapper.readValue(result.toString(), EtherpadGroupPadDto.class);
        return etherpadGroupPadDto.getData().getPadID();

    }

    public String createGroup() throws IOException {
        String sessionId = "";
        String endpoint = etherpadEndpoint;
        String url = endpoint + "/api/1/createGroup";

        //HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = createHttpsClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", etherpadAPIKey));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        ObjectMapper mapper = new ObjectMapper();
        EtherpadGroupDto etherpadGroupDto = mapper.readValue(result.toString(), EtherpadGroupDto.class);
        return etherpadGroupDto.getData().getGroupID();
    }

    public StringBuilder createPad(String padName) throws IOException {
        String sessionId = "";
        String endpoint = etherpadEndpoint;
        String url = endpoint + "/api/1/createPad";

        //HttpClient client = HttpClientBuilder.create().build();
        HttpClient client = createHttpsClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", etherpadAPIKey));
        urlParameters.add(new BasicNameValuePair("padID", padName));
        urlParameters.add(new BasicNameValuePair("text", "API"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result;
    }
}
