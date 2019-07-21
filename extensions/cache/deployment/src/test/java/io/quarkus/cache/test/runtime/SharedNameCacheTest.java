package io.quarkus.cache.test.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.test.runtime.ImplicitSimpleKeyCacheTest.CachedService;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests a cache shared between different beans.
 */
public class SharedNameCacheTest {

    private static final Object KEY = new Object();

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class).addClasses(CachedService.class, NameConsumerCachedService.class));

    @Inject
    CachedService nameProducerCachedService;

    @Inject
    NameConsumerCachedService nameConsumerCachedService;

    @Test
    public void testAllCacheAnnotations() {
        assertEquals(0, nameProducerCachedService.getCacheResultInvocations());

        // STEP 1
        // Let's start by getting something from the shared cache and cache the result by calling a @CacheResult-annotated
        // method.
        String value1 = nameProducerCachedService.cacheResult(KEY);
        assertEquals(1, nameProducerCachedService.getCacheResultInvocations());

        // STEP 2
        // If we get it from the shared cache again, the @CacheResult-annotated method should not be invoked and the result
        // should come from the cache. Since it comes from the cache, the object reference should be equal to STEP 1.
        String value2 = nameProducerCachedService.cacheResult(KEY);
        assertEquals(1, nameProducerCachedService.getCacheResultInvocations());
        assertTrue(value1 == value2);

        // STEP 3
        // Let's clear the entire cache used by the shared cache, but from another bean.
        nameConsumerCachedService.invalidateAll();

        // STEP 4
        // If we get the STEP 1 value from the shared cache again, the @CacheResult-annotated method should be invoked since
        // the cache was cleared in STEP 3. The returned object should have a different reference from STEP 1.
        String value4 = nameProducerCachedService.cacheResult(KEY);
        assertEquals(2, nameProducerCachedService.getCacheResultInvocations());
        assertTrue(value1 != value4);
    }

    @Dependent
    static class NameConsumerCachedService {

        @CacheInvalidateAll(cacheName = CachedService.CACHE_NAME)
        public void invalidateAll() {
        }
    }
}
