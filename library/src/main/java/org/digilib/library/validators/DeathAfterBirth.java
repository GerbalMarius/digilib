package org.digilib.library.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DeathAfterBirthValidator.class)
@Documented
public @interface DeathAfterBirth {
    String message() default "Death date must be after birth date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
