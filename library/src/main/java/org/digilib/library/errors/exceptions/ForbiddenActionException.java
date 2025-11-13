package org.digilib.library.errors.exceptions;

import lombok.Getter;

@Getter
public final class ForbiddenActionException extends RuntimeException {

    private final String requestUrl;

    private final Object claims;

    public ForbiddenActionException(String message, String requestUrl, Object claims) {
        super(message);
        this.requestUrl = requestUrl;
        this.claims = claims;
    }
}
