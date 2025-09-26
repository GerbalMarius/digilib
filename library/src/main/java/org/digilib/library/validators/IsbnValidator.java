package org.digilib.library.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsbnValidator implements ConstraintValidator<Isbn, String> {

    private boolean allowHyphensAndSpaces;
    private boolean allowIsbn10;
    private boolean allowIsbn13;

    @Override
    public void initialize(Isbn constraintAnnotation) {
        this.allowHyphensAndSpaces = constraintAnnotation.allowHyphensAndSpaces();
        this.allowIsbn10 = constraintAnnotation.allowIsbn10();
        this.allowIsbn13 = constraintAnnotation.allowIsbn13();
    }


    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String normalized = value;
        if (allowHyphensAndSpaces) {
            normalized = value.replaceAll("[-\\s]", "");
        }

        if (normalized.length() == 10 && allowIsbn10) {
            return isValidIsbn10(normalized);
        } else if (normalized.length() == 13 && allowIsbn13) {
            return isValidIsbn13(normalized);
        } else {
            return false;
        }
    }

    private boolean isValidIsbn10(String s) {
        if (!s.matches("\\d{9}[\\dXx]")) return false;

        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char c = s.charAt(i);
            int value;
            if (i == 9 && (c == 'X' || c == 'x')) {
                value = 10;
            } else if (Character.isDigit(c)) {
                value = c - '0';
            } else {
                return false;
            }

            sum += value * (10 - i);
        }
        return sum % 11 == 0;
    }

    private boolean isValidIsbn13(String s) {
        if (!s.matches("\\d{13}")) return false;

        int sum = 0;
        for (int i = 0; i < 13; i++) {
            int digit = s.codePointAt(i) - '0';
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        return sum % 10 == 0;
    }
}
