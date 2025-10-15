package org.digilib.library.validators.password;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.validators.chain.ConstraintHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private ConstraintHandler<String> handlerHead;

    @Override
    public void initialize(Password constraintAnnotation) {
        int minLength = constraintAnnotation.minLength();
        boolean needDigits = constraintAnnotation.needDigits();
        boolean needUpperCase = constraintAnnotation.needUpperCase();
        boolean needSpecialChar = constraintAnnotation.needSpecialChar();

        List<ConstraintHandler<String>> handlers = new ArrayList<>(4);

        // Always check min length if > 0
        if (minLength > 0) {
            handlers.add(new MinLengthHandler(minLength,
                    "Password must be at least " + minLength + " characters long"));
        }
        if (needDigits) {
            handlers.add(new DigitHandler("Password must contain at least one digit"));
        }
        if (needUpperCase) {
            handlers.add(new UpperCaseHandler("Password must contain at least one uppercase letter"));
        }
        if (needSpecialChar) {
            handlers.add(new SpecialCharHandler("Password must contain at least one special character"));
        }

        // Link the handlers
        if (!handlers.isEmpty()) {
            ConstraintHandler<String> head = handlers.getFirst();
            ConstraintHandler<String> cursor = head;
            for (int i = 1; i < handlers.size(); i++) {
                cursor.setNext(handlers.get(i));
                cursor = handlers.get(i);
            }
            this.handlerHead = head;
        }
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        return handlerHead.handle(s, constraintValidatorContext);
    }
}
