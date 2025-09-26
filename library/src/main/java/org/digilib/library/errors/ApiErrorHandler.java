package org.digilib.library.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public final class ApiErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException manve){

        Map<String, Object> validationErrors = LinkedHashMap.newLinkedHashMap(3);

        validationErrors.put("timestamp", Instant.now());
        validationErrors.put("status", HttpStatus.BAD_REQUEST.value());

        List<Map.Entry<String, String>> errors = manve.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.entry(err.getField(), err.getDefaultMessage()))
                .collect(Collectors.toList());

        validationErrors.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(validationErrors);

    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleNotSupportedMethods(HttpRequestMethodNotSupportedException mnse){
        String[] allowedMethods = mnse.getSupportedMethods();

        LinkedHashMap<String, Object> mappedErrors = Errors.orderedStatusMap(2, HttpStatus.METHOD_NOT_ALLOWED);

        mappedErrors.put("message", mnse.getMessage());
        mappedErrors.put("supportedMethods", allowedMethods);


        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(mappedErrors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException rnfe){
        LinkedHashMap<String, Object> mappedErrors = Errors.orderedStatusMap(2, HttpStatus.NOT_FOUND);

        mappedErrors.put("message", rnfe.getMessage());
        mappedErrors.put("id", rnfe.getId());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mappedErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> exceptionFallBack(Exception ex) {
        LinkedHashMap<String, Object> errorMap = Errors.orderedStatusMap(1, HttpStatus.INTERNAL_SERVER_ERROR);

        errorMap.put("message", ex.getMessage());
        errorMap.put("cause", ex.getClass());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMap);
    }
}
