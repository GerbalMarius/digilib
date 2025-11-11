package org.digilib.library.errors.exceptions;

import io.jsonwebtoken.Claims;
import lombok.Getter;

@Getter
public final class ExpiredRefreshTokenException extends RuntimeException {
    private final Claims claims;
    public ExpiredRefreshTokenException(String message, Claims claims) {
        super(message);
        this.claims = claims;
    }
}
