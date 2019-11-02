package io.quarkus.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

/**
 * When a method annotated with {@link CacheLoad} is invoked, Quarkus will compute a cache key from the method arguments (or
 * generate a key if the method has no arguments) and use it to check in the cache whether the method has been already invoked.
 * The key computation is done from all the method arguments if none of them is annotated with {@link CacheKey}, or all the
 * arguments annotated with {@link CacheKey} otherwise. If a value is found in the cache, it is returned and the annotated
 * method is never actually executed. If no value is found, the annotated method is invoked and the returned value is stored in
 * the cache using the computed or generated key.
 * <p>
 * The optional {@link #cacheName()} parameter can be used to specify the name of the cache. This is especially useful when the
 * same cache must be accessed from several classes. If the cache name is not specified, the fully qualified name of the class
 * where the annotated method is declared will be used as the default cache name.
 * <p>
 * This annotation can neither be used on a method returning {@code void} nor in combination with another Quarkus method
 * caching annotation.
 * <p>
 * If a method annotated with {@link CacheLoad} is called and returns {@code null}, a {@code NullPointerException} will be
 * thrown as null values are not supported by the cache. If you need to use this annotation on a method that may return null
 * values, you should change the method return type and wrap its return value inside an {@link java.util.Optional Optional}.
 * <p>
 * The underlying caching provider can be chosen and configured in the Quarkus {@link application.properties} file.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheLoad {

    /**
     * The name of the cache. If not specified, the fully qualified name of the class where the annotated method is declared
     * will be used as the default cache name.
     */
    @Nonbinding
    String cacheName() default "";
}
