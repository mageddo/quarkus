package io.quarkus.cache.runtime.augmented;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import io.quarkus.arc.runtime.InterceptorBindings;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.runtime.Cache;
import io.quarkus.cache.runtime.CacheKeyBuilder;
import io.quarkus.cache.runtime.CacheRepository;

public abstract class AugmentedCacheAnnotationInterceptor {

    @Inject
    CacheRepository cacheRepository;

    @Inject
    CacheKeyBuilder cacheKeyBuilder;

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
        List<Object> cacheKeyElements = new ArrayList<>();
        // If the method doesn't have any parameter, then a unique default key is generated and used.
        if (context.getParameters().length == 0) {
            cacheKeyElements.add(new DefaultCacheKey(cacheName));
        } else {
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
        }
        return cacheKeyBuilder.build(cacheKeyElements);
    }

    /**
     * A default immutable and unique cache key is generated when a method with no arguments is annotated with
     * {@link io.quarkus.cache.CacheLoad CacheLoad}, {@link io.quarkus.cache.CacheStore CacheStore} or
     * {@link io.quarkus.cache.CacheInvalidate CacheInvalidate}.
     */
    private static class DefaultCacheKey {

        private final String cacheName;

        public DefaultCacheKey(String cacheName) {
            this.cacheName = cacheName;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cacheName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof DefaultCacheKey) {
                DefaultCacheKey other = (DefaultCacheKey) obj;
                return Objects.equals(cacheName, other.cacheName);
            }
            return false;
        }
    }
}
