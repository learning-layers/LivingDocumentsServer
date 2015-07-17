package de.hska.ld.core.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class LDResponseEntity<T> extends ResponseEntity<T> {

    public LDResponseEntity(HttpStatus statusCode) {
        super((T) "", statusCode);
    }

    public LDResponseEntity(T body, HttpStatus statusCode) {
        super((T) (body == null ? "" : body), statusCode);
    }

    public LDResponseEntity(MultiValueMap<String, String> headers, HttpStatus statusCode) {
        super(headers, statusCode);
    }

    public LDResponseEntity(T body, MultiValueMap<String, String> headers, HttpStatus statusCode) {
        super(body, headers, statusCode);
    }
}
