package org.digilib.library.validators.death_date;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DeathDateValidator.class)
@Documented
public @interface DeathDate {
    String message() default "Death date must be valid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
