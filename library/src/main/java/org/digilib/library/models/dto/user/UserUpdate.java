package org.digilib.library.models.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.digilib.library.validators.password.Password;
import org.digilib.library.validators.password.PasswordMatches;

@JsonInclude(JsonInclude.Include.NON_NULL)
@PasswordMatches
public record UserUpdate(
        @Email(regexp = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
                + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$",
                message = "enter a valid email")
        String email,

        @Size(min = 3, max = 80)
        String firstName,

        @Size(min = 3, max = 80)
        String lastName,


        @Password(minLength = 10, needSpecialChar = true, needDigits = true, needUpperCase = true)
        String password,

        String passwordConfirmation
) {

    @AssertTrue(message = "At least one field must be provided for update")
    @SuppressWarnings("unused")
    public boolean isAtLeastOneFieldProvided() {
        return email != null
                || firstName != null
                || lastName != null
                || password != null;
    }
}
