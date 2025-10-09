package org.digilib.library.errors;

import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public static void notPositivePage(int pageNumber) {
        throwIf(pageNumber, "page", num -> num <= 0);
    }

    public static <T> void notValidSorts(String[] sorts, Class<? extends T> clazz) {
        if(sorts == null || sorts.length == 0) {
            throw new InvalidRequestParamException("sort args array is null or empty", "sorts", sorts);
        }

        List<String> declaredFields = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .toList();

        List<String> paramNames = new ArrayList<>(declaredFields.size());

        for(String fieldName : sorts) {
            if(!declaredFields.contains(fieldName)) {
                paramNames.add(fieldName);
            }
        }

        if(!paramNames.isEmpty()) {
            throw new InvalidRequestParamException(
                    "The supplied sorting fields are not present in entity of type " + clazz.getSimpleName(),
                    "sorts",
                    paramNames
            );
        }

    }

    private static InvalidRequestParamException invalidRequestParamException(String paramName, Object paramValue) {
        return new InvalidRequestParamException("The supplied value for " + paramName + " is not valid", paramName, paramValue);
    }
}
