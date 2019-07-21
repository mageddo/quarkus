package io.quarkus.cache.deployment;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.jboss.jandex.DotName;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheLoad;
import io.quarkus.cache.CacheStore;
import io.quarkus.cache.runtime.augmented.AugmentedCacheInvalidate;
import io.quarkus.cache.runtime.augmented.AugmentedCacheInvalidateAll;
import io.quarkus.cache.runtime.augmented.AugmentedCacheLoad;
import io.quarkus.cache.runtime.augmented.AugmentedCacheStore;

public class CacheDeploymentConstants {

    public static final DotName CACHE_LOAD = dotName(CacheLoad.class);
    public static final DotName CACHE_STORE = dotName(CacheStore.class);
    public static final DotName CACHE_INVALIDATE = dotName(CacheInvalidate.class);
    public static final DotName CACHE_INVALIDATE_ALL = dotName(CacheInvalidateAll.class);
    public static final DotName AUGMENTED_CACHE_LOAD = dotName(AugmentedCacheLoad.class);
    public static final DotName AUGMENTED_CACHE_STORE = dotName(AugmentedCacheStore.class);
    public static final DotName AUGMENTED_CACHE_INVALIDATE = dotName(AugmentedCacheInvalidate.class);
    public static final DotName AUGMENTED_CACHE_INVALIDATE_ALL = dotName(AugmentedCacheInvalidateAll.class);
    public static final List<DotName> ALL_CACHE_ANNOTATIONS = Arrays.asList(
            CACHE_LOAD, CACHE_STORE, CACHE_INVALIDATE, CACHE_INVALIDATE_ALL);
    public static final List<DotName> NON_BLOCKING_RETURN_TYPES = Arrays.asList(dotName(CompletionStage.class));

    public static final String CAFFEINE_CACHE_TYPE = "caffeine";
    public static final String CACHE_NAME_PARAMETER_NAME = "cacheName";

    private static DotName dotName(Class<?> annotationClass) {
        return DotName.createSimple(annotationClass.getName());
    }
}
