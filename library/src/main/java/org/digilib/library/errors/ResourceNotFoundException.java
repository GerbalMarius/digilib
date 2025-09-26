package org.digilib.library.errors;

import lombok.Getter;

/**
 * Used to handle http 404 errors that  return as payloads with failed id.
 */
@Getter
public final class ResourceNotFoundException extends RuntimeException {

    private final Object id;

    public ResourceNotFoundException(String message,  Object id) {
        super(message);
        this.id = id;
    }

    public static <T, ID> ResourceNotFoundException of(Class<? extends T> clazz, ID id) {
        return new ResourceNotFoundException(String.format("%s not found", clazz.getSimpleName()), id);
    }
}
