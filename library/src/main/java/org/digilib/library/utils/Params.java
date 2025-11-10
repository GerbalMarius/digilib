package org.digilib.library.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public final class Params {
    private Params(){}

    public static <T> void setIfPresent(T item, Consumer<? super T> setter) {
        setIfPresent(item,  v -> v, setter);
    }

    public static <T, R> void setIfPresent(T item,
                                           Function<? super T, ? extends R> transformFunc,
                                           Consumer<? super R> setter) {
        if(item != null) {
            setter.accept(transformFunc.apply(item));
        }
    }

}
