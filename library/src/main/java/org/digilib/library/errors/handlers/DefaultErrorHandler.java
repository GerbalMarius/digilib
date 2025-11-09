package org.digilib.library.errors.handlers;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Order() // -- Integer.MAX_VALUE
public class DefaultErrorHandler extends BaseApiErrorHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> exceptionFallback(Exception ex) {
        Map<String, Object> body = httpMap(2, HttpStatus.INTERNAL_SERVER_ERROR);
        body.put("message", ex.getMessage());
        body.put("cause", ex.getClass());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
