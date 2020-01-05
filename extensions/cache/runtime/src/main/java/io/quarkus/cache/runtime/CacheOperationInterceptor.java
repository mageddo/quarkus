package io.quarkus.cache.runtime;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

import io.quarkus.arc.runtime.InterceptorBindings;
import io.quarkus.cache.CacheKey;

@CacheOperation
@Interceptor
@Priority(PLATFORM_BEFORE)
public class CacheOperationInterceptor {

    private static final Logger LOGGER = Logger.getLogger(CacheOperationInterceptor.class);

    @Inject
    CacheRepository cacheRepository;

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        CacheOperation cacheOperation = getCacheOperation(invocationContext);
        Cache cache = cacheRepository.getCache(cacheOperation.cacheName());
        if (cacheOperation.invalidateAll()) {
            invalidateAll(cache);
        }
        if (cacheOperation.invalidate() || cacheOperation.load()) {
            Object key = getCacheKey(invocationContext, cache.getName());
            if (cacheOperation.invalidate()) {
                invalidate(cache, key);
            }
            if (cacheOperation.load()) {
                return load(cache, key, () -> invocationContext.proceed(), cacheOperation.lockTimeout());
            }
        }
        // This line can only be reached if cacheOperation.load() is false.
        return invocationContext.proceed();
    }

    private CacheOperation getCacheOperation(InvocationContext invocationContext) {
        for (Annotation annotation : InterceptorBindings.getInterceptorBindings(invocationContext)) {
            if (annotation instanceof CacheOperation) {
                return (CacheOperation) annotation;
            }
        }
        // The following exception should never be thrown.
        throw new IllegalStateException("Unable to find the @CacheOperation annotation");
    }

    private Object getCacheKey(InvocationContext context, String cacheName) {
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

    private void invalidateAll(Cache cache) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("Invalidating all entries from cache [%s]", cache.getName());
        }
        cache.invalidateAll();
    }

    private void invalidate(Cache cache, Object key) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("Invalidating entry with key [%s] from cache [%s]", key, cache.getName());
        }
        cache.invalidate(key);
    }

    private Object load(Cache cache, Object key, Callable<Object> valueLoader, long lockTimeout) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("Loading entry with key [%s] from cache [%s]", key, cache.getName());
        }
        return cache.get(key, valueLoader, lockTimeout);
    }
}
