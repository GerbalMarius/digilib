package org.digilib.library.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.models.dto.AuthorUpdateView;

import java.time.LocalDate;

public class DeathAfterBirthValidator implements ConstraintValidator<DeathAfterBirth, AuthorUpdateView> {

    @Override
    public boolean isValid(AuthorUpdateView dto, ConstraintValidatorContext context) {
        if (dto == null) return true;

        LocalDate deathDate = dto.getDeathDate();
        if (deathDate == null) return true;

        LocalDate birthDate = dto.getBirthDate() != null ? dto.getBirthDate() : dto.getExistingBirthDate();
        if (birthDate == null) return true;

        if (deathDate.isAfter(birthDate) || deathDate.isEqual(birthDate)) {
            return true;
        }


        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("deathDate")
                .addConstraintViolation();

        return false;
    }
}
