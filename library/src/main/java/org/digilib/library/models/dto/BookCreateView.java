package org.digilib.library.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.digilib.library.validators.Isbn;

import java.time.LocalDate;

public record BookCreateView(
        @NotBlank(message = "isbn must be provided")
        @Isbn(allowIsbn13 = true, message = "Number provided must be valid isbn13 number")
        String isbn,

        @NotBlank(message = "title must be provided")
        @Size(min = 13, max = 300, message = "title length must be: 13 <= title < 300")
        String title,

        @NotBlank(message = "summary must be provided")
        String summary,

        @NotBlank(message = "imageUrl mustn't be empty")
        String imageUrl,

        Integer pageCount,

        LocalDate publicationDate,

        @Size(min = 2, max = 10, message = "Language letters must be no longer than 10")
        String language,

        String edition,

        @NotNull(message = "genreId must be provided")
        Long genreId
        ) {


}
