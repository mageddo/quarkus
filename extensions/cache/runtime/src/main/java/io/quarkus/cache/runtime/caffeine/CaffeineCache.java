package io.quarkus.cache.runtime.caffeine;

import static io.quarkus.cache.runtime.NullValueConverter.fromCacheValue;
import static io.quarkus.cache.runtime.NullValueConverter.toCacheValue;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public Object get(Object key, Callable<Object> valueLoader, long lockTimeout) throws Exception {
        if (lockTimeout <= 0) {
            return fromCacheValue(cache.get(key, new MappingFunction(valueLoader)));
        }

        // The lock timeout logic starts here.

        /*
         * If the current key is not already associated with a value in the Caffeine cache, there's no way to know if the
         * current thread or another one is responsible for computing the missing value. While we want to wait at most for the
         * lock timeout delay if the value is being computed by another thread, we also want to ignore the timeout if the value
         * is being computed from the current thread. The following variable will be used to make sure we don't interrupt a
         * current-thread computation.
         */
        AtomicBoolean isCurrentThreadComputation = new AtomicBoolean();

        CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
            return fromCacheValue(cache.get(key, new MappingFunction(valueLoader, isCurrentThreadComputation)));
        });
        try {
            // First, we want to retrieve a value from the cache within the lock timeout delay.
            return future.get(lockTimeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // Timeout triggered! Is that a real one or was it caused by a current-thread computation that took longer than the
            // lock timeout delay?
            if (!isCurrentThreadComputation.get()) {
                // That's a real one! We don't want to wait any longer for the cache computation and we'll simply invoke the
                // cached method and return its result.
                // TODO: Add statistics here to monitor the timeout.
                return valueLoader.call();
            }
        }
        // If this line is reached, it means we caught a TimeoutException and ignored it because there was a current-thread
        // computation in progress. In that case, we want to wait until the computation is done no matter how long it takes.
        return future.get();
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

        private final Callable<?> valueLoader;
        private final AtomicBoolean isCurrentThreadComputation;

        public MappingFunction(Callable<?> valueLoader) {
            this(valueLoader, null);
        }

        public MappingFunction(Callable<?> valueLoader, AtomicBoolean isCurrentThreadComputation) {
            this.valueLoader = valueLoader;
            this.isCurrentThreadComputation = isCurrentThreadComputation;
        }

        @Override
        public Object apply(Object unusedArg) {
            if (isCurrentThreadComputation != null) {
                isCurrentThreadComputation.set(true);
            }
            try {
                return toCacheValue(valueLoader.call());
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
