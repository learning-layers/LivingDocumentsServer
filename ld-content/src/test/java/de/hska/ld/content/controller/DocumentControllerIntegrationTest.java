package de.hska.ld.content.controller;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hska.ld.content.persistence.domain.Document;
import de.hska.ld.content.util.RequestBuilder;
import de.hska.ld.core.AbstractIntegrationTest;
import de.hska.ld.core.AbstractIntegrationTest2;
import de.hska.ld.core.persistence.domain.User;
import de.hska.ld.core.service.UserService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.print.Doc;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import static de.hska.ld.core.fixture.CoreFixture.newUser;

public class DocumentControllerIntegrationTest extends AbstractIntegrationTest2 {

    private static final String RESOURCE_API = "/api";
    private static final String RESOURCE_DOCUMENT = "/document";
    private static final String TITLE = "Title";
    private static final String DESCRIPTION = "Description";

    @Autowired
    UserService userService;

    Document document;

    @Before
    public void setUp() throws Exception {
        User user = userService.findByUsername("user");
        document = new Document();
        document.setTitle(TITLE);
        document.setDescription(DESCRIPTION);
    }

    @Test
    public void thatCreateDocumentUsesHttpOkOnPersist() {
        ResponseEntity<Document> response = post().resource(RESOURCE_API + RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());
    }

    @Test
    public void testGETDocumentPageHttpOk() {
        ResponseEntity<Document> response = post().resource(RESOURCE_API + RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());

        String requestParamPageNumber = "page-number=0";
        String requestParamPageSize = "page-size=10";
        String requestParamSortDirection = "sort-direction=DESC";
        String requestParamSortProperty = "sort-property=createdAt";
        String combinedRequestParams =
                RequestBuilder.buildCombinedRequestParams(
                    requestParamPageNumber, requestParamPageSize, requestParamSortDirection, requestParamSortProperty
                );
        HttpRequest request = get().resource(RESOURCE_API + RESOURCE_DOCUMENT + combinedRequestParams).asUser();

        ResponseEntity<List<LinkedHashMap>> response2 = request.exec((Class<List<LinkedHashMap>>) (Class) ArrayList.class);
        long listSize = response2.getBody().size();
        /*Map<String, ArrayList> responseMap = response2.getBody().get(0);
        ObjectMapper mapper = new ObjectMapper();
        String responseString = response2.getBody().toString().replaceFirst("\\[", "");
        responseString = responseString.substring(0, responseString.length()-1);
        responseString = responseString.replace("=",":");
        Document responseDocument = null;
        try {
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            responseDocument = mapper.readValue(responseString, Document.class);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        Assert.assertEquals(HttpStatus.OK, response2.getStatusCode());
        Assert.assertTrue(listSize > 0);
    }

    @Test
    public void testRemoveDocumentHttpOk() {
        ResponseEntity<Document> response = post().resource(RESOURCE_API + RESOURCE_DOCUMENT).asUser().body(document).exec(Document.class);
        Assert.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assert.assertNotNull(response.getBody().getId());

        String URI = RESOURCE_API + RESOURCE_DOCUMENT + "/" + response.getBody().getId();
        ResponseEntity response2 = delete().resource(URI).asUser().exec();
        Assert.assertEquals(HttpStatus.OK, response2.getStatusCode());
        boolean exceptionOccured = false;
        try {
            ResponseEntity<Document> response3 = get().resource(URI).asUser().exec(Document.class);
        } catch (HttpClientErrorException e) {
            Assert.assertTrue(e.toString().contains("404 Not Found"));
            exceptionOccured = true;
        }
        if (!exceptionOccured) {
            Assert.fail();
        }
    }

}
