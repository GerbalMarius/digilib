package org.digilib.library.validators.isbn;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IsbnValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Isbn {

    String message() default "{org.digilib.library.models.validators.IsbnValidator.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Allow hyphens and spaces in the incoming value (they will be stripped before validation).
     */
    boolean allowHyphensAndSpaces() default true;

    /**
     * Allow ISBN-10 numbers (true by default).
     */
    boolean allowIsbn10() default false;

    /**
     * Allow ISBN-13 numbers (true by default).
     */
    boolean allowIsbn13() default false;
}
