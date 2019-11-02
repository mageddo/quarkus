package io.quarkus.cache.runtime.augmented;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

import java.lang.annotation.Annotation;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

import io.quarkus.cache.runtime.Cache;

@AugmentedCacheLoad
@Interceptor
@Priority(PLATFORM_BEFORE)
public class AugmentedCacheLoadInterceptor extends AugmentedCacheAnnotationInterceptor {

    private static final Logger LOGGER = Logger.getLogger(AugmentedCacheInvalidateAllInterceptor.class);

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        Cache cache = getCache(context, AugmentedCacheLoad.class);
        Object key = getCacheKey(context, cache.getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("Loading entry with key [%s] from cache [%s]", key, cache.getName());
        }
        return cache.get(key, () -> {
            Object interceptedMethodResult = context.proceed();
            if (interceptedMethodResult == null) {
                throw new NullPointerException(NULL_VALUES_NOT_SUPPORTED_MSG);
            }
            return interceptedMethodResult;
        });
    }

    @Override
    protected String getCacheName(Annotation annotation) {
        return AugmentedCacheLoad.class.cast(annotation).cacheName();
    }
}
