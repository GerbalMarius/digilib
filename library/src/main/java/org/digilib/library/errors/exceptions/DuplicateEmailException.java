package org.digilib.library.errors.exceptions;

public final class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
