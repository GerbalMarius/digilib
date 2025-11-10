package org.digilib.library.validators.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.models.dto.user.UserUpdate;

import java.util.Objects;

public class UpdatePasswordConfirmation implements ConstraintValidator<PasswordMatches, UserUpdate> {
    @Override
    public boolean isValid(UserUpdate value, ConstraintValidatorContext context) {
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
