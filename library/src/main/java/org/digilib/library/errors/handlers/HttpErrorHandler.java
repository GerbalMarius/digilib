package org.digilib.library.errors.handlers;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

import static org.digilib.library.utils.Requests.responseMap;

@RestControllerAdvice
@Order(3)
public class HttpErrorHandler  {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformed(HttpMessageNotReadableException e){
        Map<String, Object> body = responseMap(1, HttpStatus.BAD_REQUEST);
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException e){
        Map<String, Object> body = responseMap(2, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        body.put("mediaTypes", e.getSupportedMediaTypes());
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleNotSupportedMethods(HttpRequestMethodNotSupportedException e){
        Map<String, Object> body = responseMap(2, HttpStatus.METHOD_NOT_ALLOWED);
        body.put("message", e.getMessage());
        body.put("supportedMethods", e.getSupportedMethods());

        HttpHeaders headers = new HttpHeaders();
        if (e.getSupportedMethods() != null && e.getSupportedMethods().length > 0) {
            headers.add(HttpHeaders.ALLOW, String.join(", ", e.getSupportedMethods()));
        }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).headers(headers).body(body);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestParams(MissingServletRequestParameterException e){
        Map<String, Object> body = responseMap(2, HttpStatus.BAD_REQUEST);
        body.put("message", e.getMessage());
        body.put("parameter", e.getParameterName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e){
        Map<String, Object> body = responseMap(4, HttpStatus.BAD_REQUEST);
        body.put("message", e.getMessage());
        body.put("value", e.getValue());
        body.put("providedType", e.getValue() != null ? e.getValue().getClass().getSimpleName().toLowerCase() : null);
        body.put("requiredType", e.getRequiredType());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(NoResourceFoundException e){
        Map<String, Object> body = responseMap(2, HttpStatus.NOT_FOUND);
        body.put("message", e.getMessage());
        body.put("path", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body);
    }
}
