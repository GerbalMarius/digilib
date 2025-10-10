package org.digilib.library.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookUpdateView(

        @Size(min = 13, max = 300, message = "title length must be: 13 <= title <= 300")
        String title,

        String summary,

        String imageUrl,

        @Range(min = 0, message = "pageCount must be positive")
        Integer pageCount,

        LocalDate publicationDate,

        @Size(min = 2, max = 10, message = "Language letters must be between 2 and 10")
        String language,

        String edition,

        List<Long> authorIds
) {
    @AssertTrue(message = "At least one field must be provided for update")
    @SuppressWarnings("unused")
    public boolean isAtLeastOneFieldProvided() {
        return title != null
                || summary != null
                || imageUrl != null
                || pageCount != null
                || publicationDate != null
                || language != null
                || edition != null
                || authorIds != null;
    }

}
