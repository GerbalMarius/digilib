package org.digilib.library.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.digilib.library.validators.Isbn;

public record BookCreateData(
        @NotBlank(message = "isbn must be provided")
        @Isbn(allowIsbn13 = true, message = "Number provided must be valid isbn13 number")
        String isbn,

        @NotBlank(message = "title must be provided")
        @Size(min = 13, max = 300, message = "title length must be: 13 <= title < 300")
        String title) {


}
