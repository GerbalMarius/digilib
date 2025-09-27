package org.digilib.library.errors;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Errors {
    private Errors(){}

    /**
     * Creates a  hashMap with base capacity for timestamp, status code, status name.
     * @param additionalCapacity capacity for additional params to be added later.
     * @param status http status to create this map with.
     * @return  hashMap with status and timestamp filled in. This map is modifiable.
     */
    public static Map<String, Object> httpResponseMap(int additionalCapacity, HttpStatus status){
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
