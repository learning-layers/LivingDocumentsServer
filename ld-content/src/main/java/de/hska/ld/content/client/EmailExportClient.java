/*
 *  Code contributed to the Learning Layers project
 *  http://www.learning-layers.eu
 *  Development is partly funded by the FP7 Programme of the European
 *  Commission under Grant Agreement FP7-ICT-318209.
 *  Copyright (c) 2016, Karlsruhe University of Applied Sciences.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.hska.ld.content.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.content.dto.ExportPDFViaMailDto;
import de.hska.ld.core.util.Core;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class EmailExportClient {
    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private Environment env;

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

    public Boolean sendMail(ExportPDFViaMailDto exportPDFViaMailDto) throws IOException {
        String url = env.getProperty("mailBaseUrl");
        String apiKey = env.getProperty("mailApiKey");

        HttpClient client = createHttpsClient();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0");
        exportPDFViaMailDto.setApiKey(apiKey);
        if (exportPDFViaMailDto.getSubject() != null && !"".equals(exportPDFViaMailDto.getSubject())) {
            exportPDFViaMailDto.setSubject("Exported document sent by " + Core.currentUser().getEmail() + " - " + exportPDFViaMailDto.getSubject());
        } else {
            exportPDFViaMailDto.setSubject("Exported document sent by " + Core.currentUser().getEmail());
        }
        if (exportPDFViaMailDto.getMessage() != null && !"".equals(exportPDFViaMailDto.getMessage())) {
            exportPDFViaMailDto.setMessage("<i>This is an automatically generated email please answer to " + Core.currentUser().getEmail() + " and don't reply to this email directly.</i>\n\n<b>Subject: " + exportPDFViaMailDto.getSubject() + "</b>\n\nMessage: " + exportPDFViaMailDto.getMessage() + "\n\n\n ");
        } else {
            exportPDFViaMailDto.setMessage("<i>This is an automatically generated email please answer to " + Core.currentUser().getEmail() + " and don't reply to this email directly.</i>\n\n<b>Subject: " + exportPDFViaMailDto.getSubject() + "</b>\n\n\n ");
        }
        String jsonRequest = mapper.writeValueAsString(exportPDFViaMailDto);

        post.setEntity(
                new StringEntity(jsonRequest,
                        ContentType.create("application/json"))
        );

        HttpResponse response = client.execute(post);
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
            Boolean worked = mapper.readValue(result.toString(), Boolean.class);
            return worked;
        } catch (Exception e) {
            if (rd != null) {
                rd.close();
            }
            throw e;
        }
    }
}
