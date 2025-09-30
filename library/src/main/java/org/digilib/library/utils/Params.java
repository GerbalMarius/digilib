package org.digilib.library.utils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Params {
    private Params(){}

    public static <T> boolean areValidSorts(String[] sorts, Class<? extends T> clazz){

        if(sorts == null || sorts.length == 0) {
            return false;
        }

        Set<String> declaredFields = Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());

        for(String fieldName : sorts) {
            if(!declaredFields.contains(fieldName)) {
                return false;
            }
        }

        return true;
    }

    public static <T> void setIfPresent(T item, Consumer<? super T> setter){
        setIfPresent(item,  v -> v, setter);
    }

    public static <T, R> void setIfPresent(T item, Function<? super T, ? extends R> transformFunc
                                                    , Consumer<? super R> setter){
        if(item != null) {
            setter.accept(transformFunc.apply(item));
        }
    }


}
