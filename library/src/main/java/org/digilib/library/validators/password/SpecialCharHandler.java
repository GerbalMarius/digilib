package org.digilib.library.validators.password;

import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.validators.chain.BaseHandler;

import java.util.regex.Pattern;

public class SpecialCharHandler extends BaseHandler<String> {

    private final String message;

    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^A-Za-z0-9]");

    public SpecialCharHandler(String message) {
        this.message = message;
    }

    @Override
    public boolean handle(String value, ConstraintValidatorContext context) {

        if (value == null || !SPECIAL_CHAR_PATTERN.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        return callNext(value, context);
    }
}
