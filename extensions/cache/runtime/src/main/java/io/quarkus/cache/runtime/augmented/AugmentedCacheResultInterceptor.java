package io.quarkus.cache.runtime.augmented;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

import java.lang.annotation.Annotation;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

import io.quarkus.cache.runtime.Cache;

@AugmentedCacheResult
@Interceptor
@Priority(PLATFORM_BEFORE)
public class AugmentedCacheResultInterceptor extends AugmentedCacheAnnotationInterceptor {

    private static final Logger LOGGER = Logger.getLogger(AugmentedCacheResultInterceptor.class);

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        Cache cache = getCache(context, AugmentedCacheResult.class);
        Object key = getCacheKey(context, cache.getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("Loading entry with key [%s] from cache [%s]", key, cache.getName());
        }
        return cache.get(key, () -> context.proceed());
    }

    @Override
    protected String getAnnotationCacheName(Annotation annotation) {
        return AugmentedCacheResult.class.cast(annotation).cacheName();
    }
}
