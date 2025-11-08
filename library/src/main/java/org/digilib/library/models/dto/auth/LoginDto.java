package org.digilib.library.models.dto.auth;

import jakarta.validation.constraints.Email;
import org.digilib.library.validators.password.Password;

public record LoginDto(

        @Email(regexp = "^(?=.{1,64}@)[\\p{L}0-9_-]+(\\.[\\p{L}0-9_-]+)*@"
                + "[^-][\\p{L}0-9-]+(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$",
                message = "enter a valid email")
        String email,


        @Password(minLength = 10, needSpecialChar = true, needDigits = true, needUpperCase = true)
        String password
) {
}
