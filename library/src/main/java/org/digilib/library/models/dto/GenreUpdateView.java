package org.digilib.library.models.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record GenreUpdateView(

        @NotEmpty(message = "title must not be empty")
        @Size(min = 1, max = 30)
        String title
) {

}
