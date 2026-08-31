package com.hmg.role.util;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Cache<T> implements Supplier<T> {
    private T value;
    private boolean loaded;
    private final Supplier<T> supplier;

    @Override
    public T get() {
        if (!loaded) {
            synchronized (this) {
                value = supplier.get();
                loaded = true;
            }
        }
        return value;
    }
}
