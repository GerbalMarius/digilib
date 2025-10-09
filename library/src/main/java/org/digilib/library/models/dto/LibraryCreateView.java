package org.digilib.library.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.digilib.library.validators.IsbnValidator;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LibraryCreateView(

        @NotEmpty(message = "name of library must not be empty")
        @Size(min = 10, max = 200)
        String name,

        @NotEmpty(message = "address must not be empty")
        @Size(min = 10, max = 400)
        String address,

        @NotEmpty(message = "phone number must not be empty")
        @Size(min = 10, max = 30)
        String phoneNumber,

        @Email(regexp = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
                + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$",
                message = "enter a valid email")
        String email,


        List<String> isbns
) {
    @AssertTrue(message = "strings provided must be valid ISBNS")
    @SuppressWarnings("unused")
    public boolean validateIsbns() {
        return (isbns == null || isbns.isEmpty()) ||
                isbns.stream().allMatch(IsbnValidator::isValidIsbn13);
    }
}
