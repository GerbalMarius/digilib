package org.digilib.library.errors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public final class ApiErrorHandler {


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedRequestBodies(HttpMessageNotReadableException hmnre){
        Map<String, Object> malformedErrors = Errors.httpResponseMap(1, HttpStatus.BAD_REQUEST);
        malformedErrors.put("message", hmnre.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(malformedErrors);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException hmtmpe){
        Map<String, Object> mediaErrors = Errors.httpResponseMap(2, HttpStatus.UNSUPPORTED_MEDIA_TYPE);

        mediaErrors.put("mediaTypes", hmtmpe.getSupportedMediaTypes());
        mediaErrors.put("message", hmtmpe.getMessage());

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(mediaErrors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException dvi){
        Map<String, Object> malformedErrors = Errors.httpResponseMap(1, HttpStatus.UNPROCESSABLE_ENTITY);
        malformedErrors.put("message", dvi.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(malformedErrors);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException manve){

        Map<String, Object> validationErrors = HashMap.newHashMap(3);

        validationErrors.put("timestamp", Instant.now());
        validationErrors.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());

        List<Map.Entry<String, String>> errors = manve.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.entry(err.getField(), err.getDefaultMessage()))
                .toList();

        validationErrors.put("errors", errors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(validationErrors);

    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, b) -> a + "; " + b
                ));

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException mte){
        Map<String, Object> typeMismatchErrors = Errors.httpResponseMap(4, HttpStatus.BAD_REQUEST);

        typeMismatchErrors.put("message", mte.getMessage());
        typeMismatchErrors.put("value", mte.getValue());
        typeMismatchErrors.put("providedType", mte.getValue().getClass().getSimpleName().toLowerCase());
        typeMismatchErrors.put("requiredType", mte.getRequiredType());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(typeMismatchErrors);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleFallBackOnValidation(ValidationException ve){
        Map<String, Object> validationErrors = Errors.httpResponseMap(2, HttpStatus.BAD_REQUEST);
        validationErrors.put("message", ve.getMessage());
        validationErrors.put("reason", "Malformed request payload");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(validationErrors);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleNotSupportedMethods(HttpRequestMethodNotSupportedException mnse){
        String[] allowedMethods = mnse.getSupportedMethods();

        Map<String, Object> mappedErrors = Errors.httpResponseMap(2, HttpStatus.METHOD_NOT_ALLOWED);

        mappedErrors.put("message", mnse.getMessage());
        mappedErrors.put("supportedMethods", allowedMethods);

        HttpHeaders headers = new HttpHeaders();
        if (allowedMethods != null && allowedMethods.length > 0) {
            headers.add(HttpHeaders.ALLOW, String.join(", ", allowedMethods));
        }

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .headers(headers)
                .body(mappedErrors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException rnfe){
        Map<String, Object> mappedErrors = Errors.httpResponseMap(2, HttpStatus.NOT_FOUND);

        mappedErrors.put("message", rnfe.getMessage());
        mappedErrors.put("id", rnfe.getId());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mappedErrors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestParams(MissingServletRequestParameterException mse){
        Map<String, Object> mappedErrors = Errors.httpResponseMap(2, HttpStatus.BAD_REQUEST);
        mappedErrors.put("message", mse.getMessage());
        mappedErrors.put("parameter", mse.getParameterName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mappedErrors);
    }

    @ExceptionHandler(InvalidRequestParamException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequestParam(InvalidRequestParamException inp){
        Map<String, Object> mappedErrors = Errors.httpResponseMap(3, HttpStatus.BAD_REQUEST);
        mappedErrors.put("message", inp.getMessage());
        mappedErrors.put("paramName", inp.getParamName());


        mappedErrors.put("paramValue", inp.getParamValue());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mappedErrors);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> exceptionFallBack(Exception ex) {
        Map<String, Object> errorMap = Errors.httpResponseMap(2, HttpStatus.INTERNAL_SERVER_ERROR);

        errorMap.put("message", ex.getMessage());
        errorMap.put("cause", ex.getClass());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMap);
    }
}
