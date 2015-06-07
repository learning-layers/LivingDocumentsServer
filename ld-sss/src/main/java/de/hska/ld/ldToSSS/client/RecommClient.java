package de.hska.ld.ldToSSS.client;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class RecommClient{
    @Autowired
    private Environment env;

    private String token_sss;

    /**
     *
     * @param token_sss Security token
     * @throws Exception
     */
    public RecommClient(String token_sss) throws Exception{
        this.token_sss = token_sss;
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

    /*URI uri = new URIBuilder()
            .setScheme("http")
            .setHost("www.google.com")
            .setPath("/search")
            .setParameter("q", "httpclient")
            .setParameter("btnG", "Google Search")
            .setParameter("aq", "f")
            .setParameter("oq", "")
            .build();*/


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
        CloseableHttpResponse response = null;

        try {
            for (NameValuePair h : headers)
            {
                httpGet.addHeader(h.getName(), h.getValue());
            }

            response = httpclient.execute(httpGet);

            /*BufferedReader br = new BufferedReader(new InputStreamReader(
                    (response.getEntity().getContent())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }*/
            return response.getEntity();
        }finally {
            response.close();
        }
    }

    /**
     * Method for POST Requests
     *
     * @param uri URI for POST Request
     * @param headers Header with authentification information
     * @param body content to POST
     * @return returns the fully created entity
     * @throws Exception
     */
    public HttpEntity httpPOSTRequest(URI uri, List<NameValuePair> headers, HttpEntity body) throws Exception{
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpResponse response = null;

        try {
            // WORK AROUND StringEntity (BODY)
            // httpPut.setEntity(new StringEntity("{\"filters\":true}"));

            httpPost.setEntity(body);
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-Type", "application/json");

            for (NameValuePair h : headers)
            {
                httpPost.addHeader(h.getName(), h.getValue());
            }
            response = httpclient.execute(httpPost);
            return response.getEntity();

        } finally {
            response.close();
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
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(uri);
        CloseableHttpResponse response = null;

        try {
            // WORK AROUND StringEntity (BODY)
            // httpPut.setEntity(new StringEntity("{\"filters\":true}"));

            httpPut.setEntity(body);
            httpPut.addHeader("Accept", "application/json");
            httpPut.addHeader("Content-Type", "application/json");

            for (NameValuePair h : headers)
            {
                httpPut.addHeader(h.getName(), h.getValue());
            }

            response = httpclient.execute(httpPut);

            //System.out.println(response.getStatusLine());
            // do something useful with the response body
            // and ensure it is fully consumed

            if(body!=null){
                EntityUtils.consume(body);
            }

            return response.getEntity();
        } finally {
            response.close();
        }
    }

    /**
     *
     * @return
     */

    public List<NameValuePair> getTokenHeader(){
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        //nvps.add(new BasicNameValuePair("content-type", "application/json"));
        nvps.add(new BasicNameValuePair("Authorization", getToken_sss()));
        return nvps;
    }







}
