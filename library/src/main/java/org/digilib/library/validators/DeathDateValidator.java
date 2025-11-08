package org.digilib.library.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.models.dto.author.AuthorUpdateView;

import java.time.LocalDate;

public class DeathDateValidator implements ConstraintValidator<DeathDate, AuthorUpdateView> {

    @Override
    public boolean isValid(AuthorUpdateView dto, ConstraintValidatorContext context) {
        LocalDate deathDate = dto.getDeathDate();
        if (deathDate == null) return true;

        LocalDate birthDate = dto.getBirthDate() != null ? dto.getBirthDate() : dto.getExistingBirthDate();
        if (birthDate == null) return true;

        LocalDate today = LocalDate.now();

        if ((deathDate.isAfter(birthDate) || deathDate.isEqual(birthDate))
                && (deathDate.isBefore(today) || deathDate.isEqual(today))) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("deathDate")
                .addConstraintViolation();

        return false;
    }
}
