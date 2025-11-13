package org.digilib.library.errors.handlers;

import org.digilib.library.errors.exceptions.DuplicateEmailException;
import org.digilib.library.errors.exceptions.InvalidRequestParamException;
import org.digilib.library.errors.exceptions.ResourceNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static org.digilib.library.utils.Requests.responseMap;

@RestControllerAdvice
@Order(4)
public class DomainErrorHandler{

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException e){
        Map<String, Object> body = responseMap(2, HttpStatus.NOT_FOUND);
        body.put("message", e.getMessage());
        body.put("id", e.getId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException e){
        Map<String, Object> body = responseMap(1, HttpStatus.UNPROCESSABLE_ENTITY);
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    @ExceptionHandler(InvalidRequestParamException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequestParam(InvalidRequestParamException e){
        Map<String, Object> body = responseMap(3, HttpStatus.BAD_REQUEST);
        body.put("message", e.getMessage());
        body.put("paramName", e.getParamName());
        body.put("paramValue", e.getParamValue());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException e){
        Map<String, Object> body = responseMap(1, HttpStatus.UNPROCESSABLE_ENTITY);
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e){
        Map<String, Object> body = responseMap(1, HttpStatus.UNPROCESSABLE_ENTITY);
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }
}
