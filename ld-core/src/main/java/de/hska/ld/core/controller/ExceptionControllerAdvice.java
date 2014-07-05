package de.hska.ld.core.controller;

import de.hska.ld.core.exception.ApplicationError;
import de.hska.ld.core.exception.ApplicationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApplicationError> exception(ApplicationException e) {
        return new ResponseEntity<>(e.getApplicationError(), e.getHttpStatus());
    }
}
