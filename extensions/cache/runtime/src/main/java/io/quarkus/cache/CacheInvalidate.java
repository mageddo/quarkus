package io.quarkus.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/**
 * When a method annotated with {@link CacheInvalidate} is invoked, Quarkus will compute a cache key from the method arguments
 * (or generate a key if the method has no arguments) and use it to try to remove an existing entry from the cache. The key
 * computation is done from all the method arguments if none of them is annotated with {@link CacheKey}, or all the arguments
 * annotated with {@link CacheKey} otherwise. If the key does not identify any cache entry, nothing will happen.
 * <p>
 * This annotation can't be used in combination with another Quarkus method caching annotation.
 * <p>
 * The underlying caching provider can be chosen and configured in the Quarkus {@link application.properties} file.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheInvalidate {

    /**
     * The name of the cache.
     */
    @Nonbinding
    String cacheName();
}
