package org.digilib.library.errors.handlers;

import io.jsonwebtoken.ExpiredJwtException;
import org.digilib.library.errors.exceptions.AdminCodeMismatchException;
import org.digilib.library.errors.exceptions.ExpiredRefreshTokenException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Order(2)
public class SecurityErrorHandler extends BaseApiErrorHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException e){
        Map<String, Object> body = httpMap(1, HttpStatus.UNAUTHORIZED);

        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwt(ExpiredJwtException e){
        Map<String, Object> body = httpMap(1, HttpStatus.UNAUTHORIZED);

        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredRefreshToken(ExpiredRefreshTokenException e){
        Map<String, Object> body = httpMap(2, HttpStatus.UNAUTHORIZED);

        body.put("message", e.getMessage());
        body.put("claims", e.getClaims().toString());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    @ExceptionHandler(AdminCodeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleAdminCodeMismatch(AdminCodeMismatchException e){
        Map<String, Object> body = httpMap(1, HttpStatus.FORBIDDEN);
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabledUser(DisabledException e){
        Map<String, Object> body = httpMap(1, HttpStatus.FORBIDDEN);
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(AuthorizationDeniedException e){
        Map<String, Object> body = httpMap(1, HttpStatus.FORBIDDEN);
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(body);
    }
}
