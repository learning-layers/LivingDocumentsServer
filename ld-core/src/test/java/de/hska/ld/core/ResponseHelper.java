package de.hska.ld.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

public class ResponseHelper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T getBody(HttpResponse response, Class<T> clazz) throws IOException {
        HttpEntity entity = response.getEntity();
        String body = IOUtils.toString(entity.getContent(), Charset.forName("UTF-8"));
        ObjectMapper objectMapper = new ObjectMapper();
        T obj = null;
        try {
            obj = objectMapper.readValue(body, clazz);
        } catch (JsonMappingException e) {
            return null;
        }
        return obj;
    }

    public static <T> List<T> getPageList(HttpResponse response, Class<T> clazz) throws IOException {
        Object pageContent = ResponseHelper.getPageContent(response);
        return ResponseHelper.convertToListOf(clazz, pageContent);
    }

    public static Object getPageContent(HttpResponse response) throws IOException {
        Map page = ResponseHelper.getBody(response, Map.class);
        Assert.assertNotNull(page);
        Assert.assertNotNull(page.containsKey("content"));
        return page.get("content");
    }

    public static <T> List<T> convertToListOf(Class<T> classToConvertListObjectTo, Object contentToBeConverted) {
        if (contentToBeConverted != null) {
            try {
                String contentStringRepresentation = objectMapper.writeValueAsString(contentToBeConverted);
                return objectMapper.readValue(contentStringRepresentation, objectMapper.getTypeFactory().constructCollectionType(List.class, classToConvertListObjectTo));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static HttpStatus getStatusCode(HttpResponse response){
        return HttpStatus.valueOf(response.getStatusLine().getStatusCode());
    }

    public static HttpStatus getNotAuthenticatedStatus() {
        return HttpStatus.METHOD_NOT_ALLOWED;
    }
}
