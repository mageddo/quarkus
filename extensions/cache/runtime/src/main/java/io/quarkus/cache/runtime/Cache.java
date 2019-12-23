package io.quarkus.cache.runtime;

import java.util.concurrent.Callable;

public interface Cache {

    String getName();

    Object get(Object key, Callable<Object> valueLoader, boolean lockOnMiss, long lockTimeout);

    void invalidate(Object key);

    void invalidateAll();
}
