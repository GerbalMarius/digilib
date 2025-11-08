package org.digilib.library.models.dto.genre;

import jakarta.validation.constraints.NotEmpty;

public record GenreCreateView(

        @NotEmpty(message = "title must not be empty")
        String title
) {
}
