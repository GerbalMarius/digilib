package org.digilib.library.validators.password;

import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.validators.chain.BaseHandler;

public class MinLengthHandler extends BaseHandler<String> {

    private final int minLength;

    private final String message;

    public MinLengthHandler(int minLength, String message) {
        this.minLength = minLength;
        this.message = message;
    }

    @Override
    public boolean handle(String value, ConstraintValidatorContext context) {
        if (value == null || value.length() < minLength) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }
      return callNext(value, context);
    }
}
