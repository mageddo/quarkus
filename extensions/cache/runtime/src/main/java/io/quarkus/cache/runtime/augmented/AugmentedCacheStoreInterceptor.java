package io.quarkus.cache.runtime.augmented;

import static javax.interceptor.Interceptor.Priority.PLATFORM_BEFORE;

import java.lang.annotation.Annotation;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.logging.Logger;

import io.quarkus.cache.runtime.Cache;

@AugmentedCacheStore
@Interceptor
@Priority(PLATFORM_BEFORE)
public class AugmentedCacheStoreInterceptor extends AugmentedCacheAnnotationInterceptor {

    private static final Logger LOGGER = Logger.getLogger(AugmentedCacheStoreInterceptor.class);

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        Object interceptedMethodResult = context.proceed();
        if (interceptedMethodResult == null) {
            throw new NullPointerException(NULL_VALUES_NOT_SUPPORTED_MSG);
        }
        Cache cache = getCache(context, AugmentedCacheStore.class);
        Object key = getCacheKey(context, cache.getName());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("Storing entry with key [%s] into cache [%s]", key, cache.getName());
        }
        cache.put(key, interceptedMethodResult);
        return interceptedMethodResult;
    }

    @Override
    protected String getCacheName(Annotation annotation) {
        return AugmentedCacheStore.class.cast(annotation).cacheName();
    }
}
