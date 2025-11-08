package org.digilib.library.models.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.digilib.library.validators.password.Password;

public record RegisterDto(

        @NotEmpty(message = "email must be provided")
        @Email(regexp = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
                + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$",
                message = "enter a valid email")
        String email,

        @NotEmpty(message = "first name must be provided")
        @Size(min = 5, max = 80)
        String firstName,

        @NotEmpty(message = "last name must be provided")
        @Size(min = 5, max = 80)
        String lastName,

        @NotEmpty(message = "password must be provided")
        @Password(minLength = 10, needSpecialChar = true, needDigits = true, needUpperCase = true)
        String password
) {
}
