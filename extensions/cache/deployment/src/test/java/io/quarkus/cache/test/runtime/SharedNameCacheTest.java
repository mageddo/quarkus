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
import io.quarkus.cache.test.runtime.ExplicitNameImplicitSimpleKeyCacheTest.CachedService;
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
        assertEquals(0, nameProducerCachedService.getLoadInvocations());
        assertEquals(0, nameProducerCachedService.getStoreInvocations());

        // STEP 1
        // Let's start by getting something from alpha and cache the result by calling a @CacheLoad-annotated method.
        String value1 = nameProducerCachedService.load(KEY);
        assertEquals(1, nameProducerCachedService.getLoadInvocations());

        // STEP 2
        // If we get it from alpha again, the @CacheLoad-annotated method should not be invoked and the result should come from
        // the cache. Since it comes from the cache, the object reference should be equal to STEP 1.
        String value2 = nameProducerCachedService.load(KEY);
        assertEquals(1, nameProducerCachedService.getLoadInvocations());
        assertTrue(value1 == value2);

        // STEP 3
        // Let's clear the entire cache used by alpha, but from another bean.
        nameConsumerCachedService.invalidateAll();

        // STEP 4
        // If we get the STEP 1 value from alpha again, the @CacheLoad-annotated method should be invoked since the cache was
        // cleared in STEP 3. The returned object should have a different reference from STEP 1.
        String value4 = nameProducerCachedService.load(KEY);
        assertEquals(2, nameProducerCachedService.getLoadInvocations());
        assertTrue(value1 != value4);
    }

    @Dependent
    static class NameConsumerCachedService {

        @CacheInvalidateAll(cacheName = CachedService.CACHE_NAME)
        public void invalidateAll() {
        }
    }
}
