package org.digilib.library.errors;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
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

        var errors = manve.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage(),
                        (a, _) -> a
                ));

        validationErrors.put("errors", errors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(validationErrors);

    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException de){
        Map<String, Object> duplicateEmailErrors = Errors.httpResponseMap(1, HttpStatus.UNPROCESSABLE_ENTITY);
        duplicateEmailErrors.put("message", de.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(duplicateEmailErrors);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwt(ExpiredJwtException eje){
        Map<String, Object> expiredJwtErrors = Errors.httpResponseMap(1, HttpStatus.UNAUTHORIZED);
        expiredJwtErrors.put("message", eje.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(expiredJwtErrors);
    }

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredRefreshToken(ExpiredRefreshTokenException eje){
        Map<String, Object> expiredJwtErrors = Errors.httpResponseMap(2, HttpStatus.UNAUTHORIZED);
        expiredJwtErrors.put("message", eje.getMessage());
        expiredJwtErrors.put("claims", eje.getClaims());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(expiredJwtErrors);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException bce){
        Map<String, Object> badCredentialsErrors = Errors.httpResponseMap(1, HttpStatus.UNAUTHORIZED);
        badCredentialsErrors.put("message", bce.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(badCredentialsErrors);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(AuthorizationDeniedException ade){
        Map<String, Object> authorizationDeniedErrors = Errors.httpResponseMap(1, HttpStatus.FORBIDDEN);
        authorizationDeniedErrors.put("message", ade.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(authorizationDeniedErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> errors = Errors.httpResponseMap(3, HttpStatus.UNPROCESSABLE_ENTITY);

        var violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        cv -> cv.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, _) -> a)
                );

        errors.put("errors", violations);


        errors.put("timestamp", Instant.now());
        errors.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());

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
