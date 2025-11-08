package org.digilib.library.models.dto.book;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import org.digilib.library.models.Status;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookCopyUpdateView(

        @Pattern(regexp = "(\\p{javaUpperCase}{2}|\\p{javaUpperCase}{3})-(\\d{4})",
                 message = "barcodes must consist of 2 or 3 upper letters a hyphen followed by 4 digits")
        String barcode,

        Status status
) {
    @AssertTrue(message = "At least one field must be provided for update")
    @SuppressWarnings("unused")
    public boolean isAtLeastOneFieldProvided() {
        return barcode != null
                || status != null;
    }
}
