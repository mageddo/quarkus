package io.quarkus.cache.runtime;

import java.util.concurrent.Callable;

public interface Cache {

    String getName();

    Object get(Object key, Callable<Object> valueLoader, long lockTimeout) throws Exception;

    void invalidate(Object key);

    void invalidateAll();
}
