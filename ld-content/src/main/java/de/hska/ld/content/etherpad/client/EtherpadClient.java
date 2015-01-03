package de.hska.ld.content.etherpad.client;

import de.hska.ld.content.dto.EtherpadAuthorDto;
import de.hska.ld.content.dto.EtherpadGroupDto;
import de.hska.ld.content.dto.EtherpadGroupPadDto;
import de.hska.ld.content.dto.EtherpadSessionDto;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class EtherpadClient {
    public static boolean checkIfSessionStillValid(Long currentTime, String sessionId) throws IOException {
        String url = "http://localhost:9001/api/1/getSessionInfo";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", "0sDFF2pG4TDDGzO5Fik160kyw5D1lSDp"));
        urlParameters.add(new BasicNameValuePair("sessionID", sessionId));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        ObjectMapper mapper = new ObjectMapper();
        EtherpadSessionDto etherpadSessionDto = mapper.readValue(result.toString(), EtherpadSessionDto.class);
        if (etherpadSessionDto.getCode() == 1) {
            return false;
        } else {
            if (etherpadSessionDto.getData().getValidUntil() - currentTime >= 10800) {
                return true;
            } else
                return false;
        }
    }

    public static String createSession(String groupID, String authorID, Long validUntil) throws IOException {
        String sessionId = "";

        String url = "http://localhost:9001/api/1/createSession";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", "0sDFF2pG4TDDGzO5Fik160kyw5D1lSDp"));
        urlParameters.add(new BasicNameValuePair("groupID", groupID));
        urlParameters.add(new BasicNameValuePair("authorID", authorID));
        urlParameters.add(new BasicNameValuePair("validUntil", String.valueOf(validUntil)));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        ObjectMapper mapper = new ObjectMapper();
        EtherpadSessionDto etherpadSessionDto = mapper.readValue(result.toString(), EtherpadSessionDto.class);
        return etherpadSessionDto.getData().getSessionID();
    }

    public static String getReadOnlyID(String groupPadId) throws IOException {

        String url = "http://localhost:9001/api/1/getReadOnlyID";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", "0sDFF2pG4TDDGzO5Fik160kyw5D1lSDp"));
        urlParameters.add(new BasicNameValuePair("padID", groupPadId));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
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

    public static String createAuthor(String authorName) throws IOException {
        String sessionId = "";

        String url = "http://localhost:9001/api/1/createAuthor";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", "0sDFF2pG4TDDGzO5Fik160kyw5D1lSDp"));
        urlParameters.add(new BasicNameValuePair("name", authorName));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        ObjectMapper mapper = new ObjectMapper();
        EtherpadAuthorDto etherpadAuthorDto = mapper.readValue(result.toString(), EtherpadAuthorDto.class);
        return etherpadAuthorDto.getData().getAuthorID();
    }

    public static String createGroupPad(String groupId, String padName) throws IOException {
        String sessionId = "";

        String url = "http://localhost:9001/api/1/createGroupPad";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", "0sDFF2pG4TDDGzO5Fik160kyw5D1lSDp"));
        urlParameters.add(new BasicNameValuePair("groupID", groupId));
        urlParameters.add(new BasicNameValuePair("padName", padName));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        ObjectMapper mapper = new ObjectMapper();
        EtherpadGroupPadDto etherpadGroupPadDto = mapper.readValue(result.toString(), EtherpadGroupPadDto.class);
        return etherpadGroupPadDto.getData().getPadID();

    }

    public static String createGroup() throws IOException {
        String sessionId = "";

        String url = "http://localhost:9001/api/1/createGroup";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", "0sDFF2pG4TDDGzO5Fik160kyw5D1lSDp"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        ObjectMapper mapper = new ObjectMapper();
        EtherpadGroupDto etherpadGroupDto = mapper.readValue(result.toString(), EtherpadGroupDto.class);
        return etherpadGroupDto.getData().getGroupID();
    }

    public static StringBuffer createPad(String padName) throws IOException {
        String sessionId = "";

        String url = "http://localhost:9001/api/1/createPad";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("apikey", "0sDFF2pG4TDDGzO5Fik160kyw5D1lSDp"));
        urlParameters.add(new BasicNameValuePair("padID", padName));
        urlParameters.add(new BasicNameValuePair("text", "API"));

        post.setEntity(new UrlEncodedFormEntity(urlParameters));

        HttpResponse response = client.execute(post);
        System.out.println("Response Code : "
                + response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result;
    }
}
