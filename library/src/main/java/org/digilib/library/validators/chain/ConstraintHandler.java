package org.digilib.library.validators.chain;

import jakarta.validation.ConstraintValidatorContext;

public interface ConstraintHandler<T> {

    ConstraintHandler<T> setNext(ConstraintHandler<T> next);

    boolean handle(T value, ConstraintValidatorContext context);
}
