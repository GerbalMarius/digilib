package org.digilib.library.validators.password;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.digilib.library.validators.chain.ConstraintHandler;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private ConstraintHandler<String> handlerHead;

    @Override
    public void initialize(Password a) {
        int minLength = a.minLength();
        boolean needDigits = a.needDigits();
        boolean needUpperCase = a.needUpperCase();
        boolean needSpecialChar = a.needSpecialChar();

        @SuppressWarnings("unchecked")
        ConstraintHandler<String>[] handlers = (ConstraintHandler<String>[]) new ConstraintHandler[4];

        int size = 0;

        if (minLength > 0) {
            handlers[size++] = new MinLengthHandler(minLength, "Password must be at least " + minLength + " non-whitespace characters long");
        }
        if (needDigits) {
            handlers[size++] = new DigitHandler("Password must contain at least one digit");
        }
        if (needUpperCase) {
            handlers[size++] = new UpperCaseHandler("Password must contain at least one uppercase letter");
        }

        if (needSpecialChar) {
            handlers[size++] = new SpecialCharHandler("Password must contain at least one special character");
        }

        if (size > 0) {
            ConstraintHandler<String> head = handlers[0];

            var cursor = head;
            for (int i = 1; i < size; i++) {
                cursor = cursor.setNext(handlers[i]);
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
