package org.digilib.library.errors.handlers;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SecurityException;
import io.jsonwebtoken.security.SignatureException;

import jakarta.servlet.http.HttpServletResponse;
import org.digilib.library.errors.exceptions.AdminCodeMismatchException;
import org.digilib.library.errors.exceptions.ExpiredRefreshTokenException;
import org.digilib.library.utils.Cookies;
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
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException e,
                                                                    HttpServletResponse response) {
        Map<String, Object> body = httpMap(1, HttpStatus.UNAUTHORIZED);
        body.put("message", e.getMessage());

        Cookies.clearRefreshCookie(response);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", describeBearer("Bad username or password"))
                .body(body);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwt(ExpiredJwtException e) {
        Map<String, Object> body = httpMap(1, HttpStatus.UNAUTHORIZED);

        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", describeBearer("The access token has expired"))
                .body(body);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleUnsecureJwt(SecurityException e) {
        Map<String, Object> body = httpMap(1, HttpStatus.UNAUTHORIZED);

        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", describeBearer("JWT validation failed"))
                .body(body);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJwtSignature(SignatureException e) {
        Map<String, Object> body = httpMap(1, HttpStatus.UNAUTHORIZED);

        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", describeBearer("Signature verification failed"))
                .body(body);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJwt(MalformedJwtException e) {
        Map<String, Object> body = httpMap(1, HttpStatus.UNAUTHORIZED);

        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", describeBearer("Malformed JWT"))
                .body(body);
    }
    @ExceptionHandler(ExpiredRefreshTokenException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredRefreshToken(ExpiredRefreshTokenException e) {
        Map<String, Object> body = httpMap(2, HttpStatus.UNAUTHORIZED);
        body.put("message", e.getMessage());
        body.put("claims", e.getClaims().toString());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", describeBearer("Refresh token expired"))
                .body(body);
    }

    @ExceptionHandler(AdminCodeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleAdminCodeMismatch(AdminCodeMismatchException e) {
        Map<String, Object> body = httpMap(1, HttpStatus.FORBIDDEN);
        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabledUser(DisabledException e) {
        Map<String, Object> body = httpMap(1, HttpStatus.FORBIDDEN);

        body.put("message", e.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(AuthorizationDeniedException e) {
        Map<String, Object> body = httpMap(1, HttpStatus.FORBIDDEN);

        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(body);
    }

    private static String describeBearer(String description) {
        return "Bearer realm=\"digilib\", error=\"" + "invalid_token" + "\", error_description=\"" + description + "\"";
    }
}
