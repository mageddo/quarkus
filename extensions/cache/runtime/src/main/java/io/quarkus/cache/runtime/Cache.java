package io.quarkus.cache.runtime;

import java.util.concurrent.Callable;

public interface Cache {

    String getName();

    Object get(Object key, Callable<Object> mappingFunction);

    void put(Object key, Object value);

    void invalidate(Object key);

    void invalidateAll();
}