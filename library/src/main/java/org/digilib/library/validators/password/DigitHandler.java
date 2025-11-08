package org.digilib.library.validators.password;

import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.validators.chain.BaseHandler;

import java.util.regex.Pattern;

public class DigitHandler extends BaseHandler<String> {

    private final String message;

    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d", Pattern.CASE_INSENSITIVE);

    public DigitHandler(String message) {
        this.message = message;
    }

    @Override
    public boolean handle(String value, ConstraintValidatorContext context) {
        if (value == null || !DIGIT_PATTERN.matcher(value).find()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }
        return callNext(value, context);
    }
}
