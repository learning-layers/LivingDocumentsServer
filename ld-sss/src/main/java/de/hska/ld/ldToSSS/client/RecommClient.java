package de.hska.ld.ldToSSS.client;

import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.firewall.RequestRejectedException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class RecommClient{
    @Autowired
    private Environment env;

    private String token_sss;
    private static String proxyUser;
    private static String proxyPass;
    private Boolean usingProxy = false;

    /**
     *
     * @param token_sss Security token
     * @throws Exception
     */
    public RecommClient(String token_sss) throws Exception{
        this.token_sss = token_sss;
    }

    public RecommClient(String token_sss, String proxyUser, String proxyPass) throws Exception{
        this.token_sss = token_sss;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
        this.usingProxy = true;
    }

    /**
     *
     * @return returns security token
     */
    public String getToken_sss() {
        return token_sss;
    }


    /**
     *
     * @param token_sss security token
     */
    public void setToken_sss(String token_sss) {
        this.token_sss = token_sss;
    }
    
    public static String getProxyUser() {
        return proxyUser;
    }
    
    public static String getProxyPass() {
        return proxyPass;
    }

    public Boolean isUsingProxy() {
        return usingProxy;
    }
    
    /*
        URI uri = new URIBuilder()
        .setScheme("http")
        .setHost("www.google.com")
        .setPath("/search")
        .setParameter("q", "httpclient")
        .setParameter("btnG", "Google Search")
        .setParameter("aq", "f")
        .setParameter("oq", "")
        .build();
    */
    /**
     * Method for GET Requests
     *
     * @param uri URI for GET Request
     * @param headers Header with authentification information
     * @return  Response is the found entity object
     * @throws Exception
     */
    public HttpEntity httpGETRequest(URI uri, List<NameValuePair> headers) throws Exception{
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uri);
        HttpResponse response;

        for (NameValuePair h : headers)
        {
            httpGet.addHeader(h.getName(), h.getValue());
        }

         if(isUsingProxy()) {
             response = requestWithProxy(httpGet);
         }else{
             response = request(httpGet);
         }

         if(response!=null) {
             if(response.getStatusLine().getStatusCode() != 500) {
                 return response.getEntity();
             }else{
                 throw new RequestRejectedException("Social Semantic Server couldn't process GET request");
             }
         }

        throw new TimeoutException("There was an error connecting LDocs to SSS (GET request), please try again later");
    }


    /**
     * Method for POST Request
     *
     * @param uri URI for POST Request
     * @param headers Header with authentification information
     * @param body POST content sent to Server
     * @return Response is the created entity object
     * @throws Exception
     */
    public HttpEntity httpPOSTRequest(URI uri, List<NameValuePair> headers, HttpEntity body) throws Exception{
        HttpPost httpPost = new HttpPost(uri);
        HttpResponse response;
        try {
            httpPost.setEntity(body);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-Type", "application/json");

            for (NameValuePair h : headers) {
                httpPost.addHeader(h.getName(), h.getValue());
            }
            if (isUsingProxy()) {
                response = requestWithProxy(httpPost);
            } else {
                response = request(httpPost);
            }
            if (response != null) {
                if (response.getStatusLine().getStatusCode() != 500) {
                    return response.getEntity();
                } else {
                    throw new RequestRejectedException("Social Semantic Server couldn't process POST request");
                }
            }
            throw new TimeoutException("There was an error connecting LDocs to SSS (POST request), please try again later");
        }finally {
            if(body!=null){
                EntityUtils.consume(body);
            }
        }
    }

    /**
     * Method for PUT Requests
     *
     * @param uri URI for PUT Request
     * @param headers Header with authentification information
     * @param body content to PUT
     * @return returns the fully updated entity
     * @throws Exception
     */
    public HttpEntity httpPUTRequest(URI uri, List<NameValuePair> headers, HttpEntity body) throws Exception{
        HttpPut httpPut = new HttpPut(uri);
        HttpResponse response;

        try {
            httpPut.setEntity(body);
            httpPut.addHeader("Accept", "application/json");
            httpPut.addHeader("Content-Type", "application/json");

            for (NameValuePair h : headers) {
                httpPut.addHeader(h.getName(), h.getValue());
            }

            if (isUsingProxy()) {
                response = requestWithProxy(httpPut);
            } else {
                response = request(httpPut);
            }

            if (response != null) {
                if (response.getStatusLine().getStatusCode() != 500) {
                    return response.getEntity();
                } else {
                    throw new RequestRejectedException("Social Semantic Server couldn't process PUT request");
                }
            }
            throw new TimeoutException("There was an error connecting LDocs to SSS (PUT request), please try again later");
        }finally {
            if(body!=null){
                EntityUtils.consume(body);
            }
        }
    }

    /**
     *
     * @return returns authorization token
     */
    public List<NameValuePair> getTokenHeader(){
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        //nvps.add(new BasicNameValuePair("content-type", "application/json"));
        nvps.add(new BasicNameValuePair("Authorization", getToken_sss()));
        return nvps;
    }


    ////////////////////////////////////////////////////////////////////
    ///////////////////////////PROXY NETWORK SETUP//////////////////////
    ////////////////////////////////////////////////////////////////////
    //IMPORTANT This is used whenever we test our module within the Hochschule Karlsruhe

    /**
     *
     * @param request
     * @return returns HTTP Response through proxy
     */
    public HttpResponse requestWithProxy(HttpRequestBase request) {
        String USERNAME = getProxyUser(); // username for proxy authentication
        String PASSWORD = getProxyPass(); // password for proxy authentication

        HttpResponse resp = null;
        String PROXY_ADDRESS = "proxy.hs-karlsruhe.de"; // proxy (IP) address
        String PROXY_DOMAIN = "http"; // proxy domain

        HttpHost proxy = new HttpHost(PROXY_ADDRESS, 8888);
        DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

        AuthCache authCache = new BasicAuthCache();
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(PROXY_ADDRESS, 8888), new NTCredentials(USERNAME, PASSWORD, "", PROXY_DOMAIN));
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        String token = USERNAME + ":" + PASSWORD;
        String auth = "Basic " + new String(Base64.encode(token.getBytes()));
        request.addHeader("Proxy-Authorization", auth);
        request.addHeader("Https-Proxy-Authorization", auth);

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setStaleConnectionCheckEnabled(true)
                .build();

        //Main client about to connect
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .setRoutePlanner(routePlanner)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();

        try {
            resp = httpclient.execute(request, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }

    /**
     *
     * @param request HTTP Request
     * @return returns HTTP Respons without proxy
     */
    public HttpResponse request(HttpRequestBase request){
        HttpResponse resp = null;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setStaleConnectionCheckEnabled(true)
                .build();

        //Main client about to connect
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();

        try {
            resp = httpclient.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resp;
    }













}
