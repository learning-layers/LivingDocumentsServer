package de.hska.ld.oidc.client;

import de.hska.ld.content.dto.SSSAuthDto;
import de.hska.ld.core.exception.ValidationException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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
            ObjectMapper mapper = new ObjectMapper();
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
}
