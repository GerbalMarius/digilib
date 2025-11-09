package org.digilib.library.errors;

import io.jsonwebtoken.Claims;
import lombok.Getter;

@Getter
public class ExpiredRefreshTokenException extends RuntimeException {
    private final Claims claims;
    public ExpiredRefreshTokenException(String message, Claims claims) {
        super(message);
        this.claims = claims;
    }
}
