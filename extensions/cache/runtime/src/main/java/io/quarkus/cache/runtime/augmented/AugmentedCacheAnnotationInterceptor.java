package io.quarkus.cache.runtime.augmented;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import io.quarkus.arc.runtime.InterceptorBindings;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.runtime.Cache;
import io.quarkus.cache.runtime.CacheKeyBuilder;
import io.quarkus.cache.runtime.CacheRepository;

public abstract class AugmentedCacheAnnotationInterceptor {

    public static final String NULL_VALUES_NOT_SUPPORTED_MSG = "Null values are not supported by the Quarkus application data cache";

    @Inject
    CacheRepository cacheRepository;

    protected abstract String getCacheName(Annotation annotation);

    protected Cache getCache(InvocationContext context, Class<?> cacheAnnotation) {
        String cacheName = resolveCacheName(context, cacheAnnotation);
        return cacheRepository.getCache(cacheName);
    }

    private String resolveCacheName(InvocationContext context, Class<?> cacheAnnotationClass) {
        for (Annotation annotation : InterceptorBindings.getInterceptorBindings(context)) {
            if (cacheAnnotationClass.isInstance(annotation)) {
                return getCacheName(annotation);
            }
        }
        throw new IllegalStateException("Unable to determine the cache name. Please report this to the development team.");
    }

    protected Object getCacheKey(InvocationContext context, String cacheName) {
        // If the method doesn't have any parameter, then a unique default key is generated and used.
        if (context.getParameters().length == 0) {
            return CacheKeyBuilder.buildDefault(cacheName);
        } else {
            List<Object> cacheKeyElements = new ArrayList<>();
            // If at least one of the method parameters is annotated with @CacheKey, then the key is composed of all
            // @CacheKey-annotated parameters.
            for (int i = 0; i < context.getParameters().length; i++) {
                if (context.getMethod().getParameters()[i].isAnnotationPresent(CacheKey.class)) {
                    cacheKeyElements.add(context.getParameters()[i]);
                }
            }
            // If there's no @CacheKey-annotated parameter, then the key is composed of all of the method parameters.
            if (cacheKeyElements.isEmpty()) {
                cacheKeyElements.addAll(Arrays.asList(context.getParameters()));
            }
            return CacheKeyBuilder.build(cacheKeyElements);
        }
    }
}
