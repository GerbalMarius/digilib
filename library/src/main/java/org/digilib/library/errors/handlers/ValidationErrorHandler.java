package org.digilib.library.errors.handlers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.digilib.library.utils.Requests.responseMap;

@RestControllerAdvice
@Order(1)
public class ValidationErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException manve){
        Map<String, Object> body = responseMap(1, HttpStatus.UNPROCESSABLE_ENTITY);

        var errors = manve.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        field -> Objects.toString(field.getDefaultMessage()),
                        (a, _) -> a
                ));

        body.put("errors", errors);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> body = responseMap(1, HttpStatus.UNPROCESSABLE_ENTITY);

        var violations = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        cv -> cv.getPropertyPath().toString(),
                        ConstraintViolation::getMessage,
                        (a, _) -> a
                ));

        body.put("errors", violations);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(body);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationFallback(ValidationException ve){
        Map<String, Object> body = responseMap(2, HttpStatus.BAD_REQUEST);

        body.put("message", ve.getMessage());
        body.put("reason", "Malformed request payload");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body);
    }
}
