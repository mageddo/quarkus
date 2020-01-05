package io.quarkus.cache.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface CacheOperation {

    @Nonbinding
    String cacheName() default "";

    @Nonbinding
    boolean invalidateAll() default false;

    @Nonbinding
    boolean invalidate() default false;

    @Nonbinding
    boolean load() default false;

    @Nonbinding
    long lockTimeout() default 0;
}
