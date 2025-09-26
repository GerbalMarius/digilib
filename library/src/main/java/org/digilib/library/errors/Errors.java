package org.digilib.library.errors;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.LinkedHashMap;

public final class Errors {
    private Errors(){}

    /**
     * Creates a linked hashMap with base capacity for timestamp, status code, status name.
     * @param additionalCapacity capacity for additional params to be added later.
     * @param status http status to create this map with.
     * @return linked hashMap with status and timestamp filled in the map is modifiable.
     */
    public static LinkedHashMap<String, Object> orderedStatusMap(int additionalCapacity, HttpStatus status){
        LinkedHashMap<String, Object> mappedErrors
                = LinkedHashMap.newLinkedHashMap(
                        Math.max(0, additionalCapacity) + 3
        );

        mappedErrors.put("timestamp", Instant.now());

        mappedErrors.put("status", status.value());

        mappedErrors.put("error", status);

        return mappedErrors;
    }
}
