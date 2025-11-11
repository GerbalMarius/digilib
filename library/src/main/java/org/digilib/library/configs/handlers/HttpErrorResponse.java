package org.digilib.library.configs.handlers;

import org.springframework.http.HttpStatus;

import java.time.Instant;

import static org.digilib.library.LibraryApplication.BACK_URL;

 record HttpErrorResponse(
        Instant timeStamp,
        int status,
        String error,

        String message,

        String path
) {
     static HttpErrorResponse of(HttpStatus httpStatus, String message, String path) {
        return new HttpErrorResponse(
                Instant.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                message,
                BACK_URL + path
        );
    }
}
