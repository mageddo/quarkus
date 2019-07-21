package io.quarkus.cache.runtime.caffeine;

import static io.quarkus.cache.runtime.NullValueConverter.fromCacheValue;
import static io.quarkus.cache.runtime.NullValueConverter.toCacheValue;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Caffeine;

import io.quarkus.cache.runtime.Cache;

public class CaffeineCache implements Cache {

    private com.github.benmanes.caffeine.cache.Cache<Object, Object> cache;

    private String name;

    private Integer initialCapacity;

    private Long maximumSize;

    private Duration expireAfterWrite;

    private Duration expireAfterAccess;

    public CaffeineCache(CaffeineCacheInfo cacheInfo) {
        this.name = cacheInfo.name;
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        // TODO: The following line is a workaround for a GraalVM issue: https://github.com/oracle/graal/issues/1524
        // It should be removed as soon as a Quarkus release depends on GraalVM 19.3.0 or greater.
        builder.executor(Runnable::run);
        if (cacheInfo.initialCapacity != null) {
            this.initialCapacity = cacheInfo.initialCapacity;
            builder.initialCapacity(cacheInfo.initialCapacity);
        }
        if (cacheInfo.maximumSize != null) {
            this.maximumSize = cacheInfo.maximumSize;
            builder.maximumSize(cacheInfo.maximumSize);
        }
        if (cacheInfo.expireAfterWrite != null) {
            this.expireAfterWrite = cacheInfo.expireAfterWrite;
            builder.expireAfterWrite(cacheInfo.expireAfterWrite);
        }
        if (cacheInfo.expireAfterAccess != null) {
            this.expireAfterAccess = cacheInfo.expireAfterAccess;
            builder.expireAfterAccess(cacheInfo.expireAfterAccess);
        }
        cache = builder.build();
    }

    @Override
    public Object get(Object key, Callable<Object> mappingFunction) {
        return fromCacheValue(cache.get(key, new MappingFunction(mappingFunction)));
    }

    @Override
    public void put(Object key, Object value) {
        cache.put(key, toCacheValue(value));
    }

    @Override
    public void invalidate(Object key) {
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public String getName() {
        return name;
    }

    // For testing purposes only.
    public Integer getInitialCapacity() {
        return initialCapacity;
    }

    // For testing purposes only.
    public Long getMaximumSize() {
        return maximumSize;
    }

    // For testing purposes only.
    public Duration getExpireAfterWrite() {
        return expireAfterWrite;
    }

    // For testing purposes only.
    public Duration getExpireAfterAccess() {
        return expireAfterAccess;
    }

    private static class MappingFunction implements Function<Object, Object> {

        private final Callable<?> mappingFunction;

        public MappingFunction(Callable<?> mappingFunction) {
            this.mappingFunction = mappingFunction;
        }

        @Override
        public Object apply(Object unusedArg) {
            try {
                return toCacheValue(mappingFunction.call());
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
