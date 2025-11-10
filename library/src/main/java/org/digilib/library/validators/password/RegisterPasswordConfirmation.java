package org.digilib.library.validators.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.models.dto.auth.RegisterDto;

import java.util.Objects;

public class RegisterPasswordConfirmation implements ConstraintValidator<PasswordMatches, RegisterDto> {

    @Override
    public boolean isValid(RegisterDto value, ConstraintValidatorContext context) {
        if (Objects.equals(value.password(), value.passwordConfirmation())) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("passwordConfirmation")
                .addConstraintViolation();

        return false;
    }
}
