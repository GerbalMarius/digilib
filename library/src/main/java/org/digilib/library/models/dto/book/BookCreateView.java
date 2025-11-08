package org.digilib.library.models.dto.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.digilib.library.validators.Isbn;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;
import java.util.List;

public record BookCreateView(
        @NotBlank(message = "isbn must be provided")
        @Isbn(allowIsbn13 = true, message = "Number provided must be valid isbn13 number")
        String isbn,

        @NotBlank(message = "title must be provided")
        @Size(min = 13, max = 300)
        String title,

        @NotBlank(message = "summary must be provided")
        String summary,

        @NotBlank(message = "imageUrl mustn't be empty")
        String imageUrl,

        @Range(min = 0, message = "pageCount must be positive")
        Integer pageCount,

        LocalDate publicationDate,

        @Size(min = 2, max = 10, message = "Language letters must be no longer than 10")
        String language,

        String edition,

        @NotNull(message = "genreId must be provided")
        Long genreId,

        @NotEmpty(message = "at least one author must be provided")
        List<Long> authorIds
        ) {


}
