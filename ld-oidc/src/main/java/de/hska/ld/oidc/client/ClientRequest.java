package de.hska.ld.oidc.client;

import de.hska.ld.core.config.ApplicationContextProvider;
import de.hska.ld.core.logging.ExceptionLogger;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ClientRequest {
    protected ExceptionLogger exceptionLogger;
    protected HttpClient client;
    protected HttpResponse response;
    protected String url;

    public ClientRequest(String url) {
        this.exceptionLogger = ApplicationContextProvider.getApplicationContextStatic().getBean("exceptionLogger", ExceptionLogger.class);
        this.url = url;
        this.client = getHttpClientFor(url);
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
}
