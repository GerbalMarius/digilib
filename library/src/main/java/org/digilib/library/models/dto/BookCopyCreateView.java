package org.digilib.library.models.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.digilib.library.validators.Isbn;

public record BookCopyCreateView(

        @NotEmpty(message = "bookIsbn must be provided")
        @Isbn(allowIsbn13 = true, message = "provided string must be a valid isbn 13 number")
        String bookIsbn,

        @NotNull(message = "barcode must be provided")
        @Pattern(regexp = "(\\p{javaUpperCase}{2}|\\p{javaUpperCase}{3})-(\\d{4})",
                 message = "barcodes must consist of 2 or 3 upper letters a hyphen followed by 4 digits")
        String barcode
) {
}
