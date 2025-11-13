package org.digilib.library.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public final class Requests {

    private Requests(){}

    /**
     * Returns the request path including query string.
     * @param request current request.
     * @return request path including query string, if any, else empty string.
     */
    public static String requestPath(HttpServletRequest request) {
        String query = request.getQueryString() == null ? ""
                : "?" + request.getQueryString();

       return request.getRequestURL().toString() + query;
    }

    /**
     * Creates a hashMap with base capacity for timestamp, status code, status name.
     * @param additionalCapacity capacity for additional params to be added later.
     * @param status http status to create this map with.
     * @return  hashMap with status and timestamp filled in. This map is modifiable.
     */
    public static Map<String, Object> responseMap(int additionalCapacity, HttpStatus status) {
        HashMap<String, Object> mappedErrors
                = HashMap.newHashMap(
                Math.max(0, additionalCapacity) + 3
        );

        mappedErrors.put("timestamp", Instant.now());

        mappedErrors.put("status", status.value());

        mappedErrors.put("error", status);

        return mappedErrors;
    }
}
