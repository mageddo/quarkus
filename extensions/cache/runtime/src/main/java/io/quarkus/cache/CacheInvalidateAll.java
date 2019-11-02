package io.quarkus.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/**
 * When a method annotated with {@link CacheInvalidateAll} is invoked, Quarkus will remove all entries from the cache.
 * <p>
 * The optional {@link #cacheName()} parameter can be used to specify the name of the cache. This is especially useful when the
 * same cache must be accessed from several classes. If the cache name is not specified, the fully qualified name of the class
 * where the annotated method is declared will be used as the default cache name.
 * <p>
 * This annotation can't be used in combination with another Quarkus method caching annotation.
 * <p>
 * The underlying caching provider can be chosen and configured in the Quarkus {@link application.properties} file.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheInvalidateAll {

    /**
     * The name of the cache. If not specified, the fully qualified name of the class where the annotated method is declared
     * will be used as the default cache name.
     */
    @Nonbinding
    String cacheName() default "";
}
