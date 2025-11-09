package org.digilib.library.errors.handlers;

import org.digilib.library.errors.Errors;
import org.springframework.http.HttpStatus;

import java.util.Map;

public abstract class BaseApiErrorHandler {
    protected Map<String, Object> httpMap(int code, HttpStatus status) {
        return Errors.httpResponseMap(code, status);
    }
}