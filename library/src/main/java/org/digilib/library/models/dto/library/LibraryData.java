package org.digilib.library.models.dto.library;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.digilib.library.models.Library;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LibraryData(
        long id,

        String name,

        String address,

        String phoneNumber,

        String email
) {
    public static LibraryData wrapLibrary(Library library) {
        return new LibraryData(
                library.getId(),
                library.getName(),
                library.getAddress(),
                library.getPhoneNumber(),
                library.getEmail()
        );
    }
}
