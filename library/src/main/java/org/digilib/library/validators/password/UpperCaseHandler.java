package org.digilib.library.validators.password;

import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.validators.chain.BaseHandler;

import java.util.regex.Pattern;

public class UpperCaseHandler extends BaseHandler<String> {

    private final String message;

    private static final Pattern UPPERCASE = Pattern.compile("\\p{Upper}");

    public UpperCaseHandler(String message) {
        this.message = message;
    }

    @Override
    public boolean handle(String value, ConstraintValidatorContext context) {
        if (value == null || UPPERCASE.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }
        return callNext(value, context);
    }
}
