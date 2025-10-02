package org.digilib.library.models.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AuthorCreateView(

        @NotEmpty(message = "firstName must be provided")
        @Size(min = 10, max =  50, message = "firstName length must be between 10 and 50")
        String firstName,

        @NotEmpty(message = "lastName must be provided")
        @Size(min = 10, max = 80, message = "lastName length must be between 10 and 80")
        String lastName,

        @NotNull(message = "birthDate must be provided")
        LocalDate birthDate,

        LocalDate deathDate,

        @NotNull(message = "genreId must be provided")
        Long genreId
) {
}
