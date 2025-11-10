package org.digilib.library.validators.password;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.validators.chain.ConstraintHandler;

import java.util.ArrayList;
import java.util.List;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private ConstraintHandler<String> handlerHead;

    @Override
    public void initialize(Password a) {
        int minLength = a.minLength();
        boolean needDigits = a.needDigits();
        boolean needUpperCase = a.needUpperCase();
        boolean needSpecialChar = a.needSpecialChar();

        List<ConstraintHandler<String>> handlers = new ArrayList<>(4);

        if (minLength > 0) {
            handlers.add(new MinLengthHandler(minLength, "Password must be at least " + minLength + " non-whitespace characters long"));
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

        if (!handlers.isEmpty()) {
            ConstraintHandler<String> head = handlers.getFirst();
            ConstraintHandler<String> cursor = head;
            for (int i = 1; i < handlers.size(); i++) {
                cursor = cursor.setNext(handlers.get(i));
            }
            this.handlerHead = head;
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (handlerHead == null) return true;

        if (value == null || value.isBlank()) return true;

        return handlerHead.handle(value.trim(), context);
    }
}
