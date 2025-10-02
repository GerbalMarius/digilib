package org.digilib.library.errors;

import lombok.Getter;
import org.digilib.library.utils.Params;

import java.util.function.IntPredicate;
import java.util.function.Predicate;

@Getter
public final class InvalidRequestParamException extends RuntimeException {

    private String paramName;

    private Object paramValue;

    public InvalidRequestParamException(String message) {
        super(message);
    }

    public InvalidRequestParamException(String message, String paramName, Object paramValue) {
        this(message);
        this.paramName = paramName;
        this.paramValue = paramValue;
    }

    public static void throwIf(int value, String paramName, IntPredicate predicate){
        if (predicate.test(value)) {
           throw  invalidRequestParamException(paramName, value);
        }
    }

    public static <T> void throwIf(T value, String paramName, Predicate<? super T> predicate){
        if (predicate.test(value)) {
            throw  invalidRequestParamException(paramName, value);
        }
    }

    public static void negativePage(int pageNumber) {
        throwIf(pageNumber, "page", num -> num <= 0);
    }

    public static <T> void notValidSorts(String[] sorts, Class<? extends T> clazz) {
        throwIf(sorts, "sorts", strings -> Params.invalidSorts(strings, clazz));
    }

    private static InvalidRequestParamException invalidRequestParamException(String paramName, Object paramValue) {
        return new InvalidRequestParamException("The supplied value for " + paramName + " is not valid", paramName, paramValue);
    }
}
