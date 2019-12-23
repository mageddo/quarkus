package io.quarkus.cache.runtime.augmented;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

@InterceptorBinding
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface AugmentedCacheResult {

    @Nonbinding
    String cacheName() default "";

    @Nonbinding
    boolean lockOnMiss() default false;

    @Nonbinding
    long lockTimeout() default 0;
}
