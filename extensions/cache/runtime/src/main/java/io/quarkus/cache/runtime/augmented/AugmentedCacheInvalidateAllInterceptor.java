package io.quarkus.cache.runtime.augmented;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

import java.lang.annotation.Annotation;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

import io.quarkus.cache.runtime.Cache;

@AugmentedCacheInvalidateAll
@Interceptor
@Priority(PLATFORM_BEFORE)
public class AugmentedCacheInvalidateAllInterceptor extends AugmentedCacheAnnotationInterceptor {

    private static final Logger LOGGER = Logger.getLogger(AugmentedCacheInvalidateAllInterceptor.class);

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        Cache cache = getCache(context, AugmentedCacheInvalidateAll.class);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("Invalidating all entries from cache [%s]", cache.getName());
        }
        cache.invalidateAll();
        return context.proceed();
    }

    @Override
    protected String getCacheName(Annotation annotation) {
        return AugmentedCacheInvalidateAll.class.cast(annotation).cacheName();
    }
}
