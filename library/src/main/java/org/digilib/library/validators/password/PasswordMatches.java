package org.digilib.library.validators.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {RegisterPasswordConfirmation.class, UpdatePasswordConfirmation.class})
public @interface PasswordMatches {

    String message() default "passwords do not match";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
