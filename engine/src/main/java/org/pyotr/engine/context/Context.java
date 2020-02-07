package org.pyotr.engine.context;

import java.util.Map;

import com.google.common.collect.Maps;

public class Context {

    private final Map<Class<? extends Object>, Object> map = Maps.newConcurrentMap();

    public <T> T get(Class<? extends T> type) {
        return type.cast(map.get(type));
    }

    public <T, U extends T> void put(Class<T> type, U object) {
        map.put(type, object);
    }
}