package org.digilib.library.validators.chain;

import jakarta.validation.ConstraintValidatorContext;

public abstract class BaseHandler<T> implements ConstraintHandler<T> {

    private ConstraintHandler<T> next;

    @Override
    public ConstraintHandler<T> setNext(ConstraintHandler<T> next) {
        this.next = next;
        return next;
    }

    protected boolean callNext(T value, ConstraintValidatorContext context) {
        return next == null || next.handle(value, context);
    }
}
