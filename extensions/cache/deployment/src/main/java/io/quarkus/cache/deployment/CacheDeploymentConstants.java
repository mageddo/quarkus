package io.quarkus.cache.deployment;

import java.util.Arrays;
import java.util.List;

import org.jboss.jandex.DotName;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.cache.runtime.CacheOperation;

public class CacheDeploymentConstants {

    public static final DotName CACHE_RESULT = dotName(CacheResult.class);
    public static final DotName CACHE_INVALIDATE = dotName(CacheInvalidate.class);
    public static final DotName CACHE_INVALIDATE_ALL = dotName(CacheInvalidateAll.class);
    public static final DotName CACHE_OPERATION = dotName(CacheOperation.class);
    public static final List<DotName> ALL_CACHE_ANNOTATIONS = Arrays.asList(CACHE_RESULT, CACHE_INVALIDATE,
            CACHE_INVALIDATE_ALL);
    public static final String CACHE_NAME_PARAMETER_NAME = "cacheName";
    public static final String CAFFEINE_CACHE_TYPE = "caffeine";

    private static DotName dotName(Class<?> annotationClass) {
        return DotName.createSimple(annotationClass.getName());
    }
}
