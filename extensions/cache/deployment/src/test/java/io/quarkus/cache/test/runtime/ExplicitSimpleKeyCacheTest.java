package io.quarkus.cache.test.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheKey;
import io.quarkus.cache.CacheResult;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests a cache with <b>explicit simple</b> cache keys.<br>
 * All methods with a {@link CacheKey @CacheKey}-annotated argument also have another argument which is not part of the key.
 */
public class ExplicitSimpleKeyCacheTest {

    private static final long KEY_1 = 123L;
    private static final long KEY_2 = 456L;

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class).addClass(CachedService.class));

    @Inject
    CachedService cachedService;

    @Test
    public void testAllCacheAnnotations() {
        assertEquals(0, cachedService.getCacheResultInvocations());

        // In most of the cached service methods calls below, a changing second argument will be passed to the methods.
        // The fact that it changes each time should not have any effect on the cache because it is not part of the cache key.

        // STEP 1
        // Let's start by getting something and cache the result by calling a @CacheResult-annotated method.
        String value1 = cachedService.cacheResult(KEY_1, new Object());
        assertEquals(1, cachedService.getCacheResultInvocations());

        // STEP 2
        // If we get it again, the @CacheResult-annotated method should not be invoked and the result should come from the cache.
        // Since it comes from the cache, the object reference should be equal to STEP 1.
        String value2 = cachedService.cacheResult(KEY_1, new Object());
        assertEquals(1, cachedService.getCacheResultInvocations());
        assertTrue(value1 == value2);

        // STEP 3
        // If the key is changed, another result should be returned from the @CacheResult-annotated method invocation.
        String value3 = cachedService.cacheResult(KEY_2, new Object());
        assertEquals(2, cachedService.getCacheResultInvocations());
        assertTrue(value2 != value3);

        // STEP 4
        // Now, we want to delete the STEP 2 entry from the cache, but keep the STEP 3 entry.
        cachedService.invalidate(KEY_1, new Object());

        // STEP 5
        // If we try to get the STEP 2 entry, the @CacheResult-annotated method should be invoked and return a new object.
        String value5 = cachedService.cacheResult(KEY_1, new Object());
        assertEquals(3, cachedService.getCacheResultInvocations());
        assertTrue(value2 != value5);

        // STEP 6
        // But if we try to get the STEP 3 entry, it should come from the cache with no method invocation.
        String value6 = cachedService.cacheResult(KEY_2, new Object());
        assertEquals(3, cachedService.getCacheResultInvocations());
        assertTrue(value3 == value6);

        // STEP 7
        // Almost done, let's clear the entire cache.
        cachedService.invalidateAll();

        // STEP 8
        // If we try to get the STEP 5 entry, the @CacheResult-annotated method should be invoked and return a new object.
        String value8 = cachedService.cacheResult(KEY_1, new Object());
        assertEquals(4, cachedService.getCacheResultInvocations());
        assertTrue(value5 != value8);

        // STEP 9
        // If we try to get the STEP 6 entry, the @CacheResult-annotated method should be invoked and return a new object.
        String value9 = cachedService.cacheResult(KEY_2, new Object());
        assertEquals(5, cachedService.getCacheResultInvocations());
        assertTrue(value6 != value9);
    }

    @Dependent
    static class CachedService {

        private static final String CACHE_NAME = "explicitSimpleKeyCache";

        private int cacheResultInvocations;

        @CacheResult(cacheName = CACHE_NAME)
        public String cacheResult(@CacheKey long key, Object notPartOfTheKey) {
            cacheResultInvocations++;
            return new String();
        }

        @CacheInvalidate(cacheName = CACHE_NAME)
        public void invalidate(@CacheKey long key, Object notPartOfTheKey) {
        }

        @CacheInvalidateAll(cacheName = CACHE_NAME)
        public void invalidateAll() {
        }

        public int getCacheResultInvocations() {
            return cacheResultInvocations;
        }
    }
}
