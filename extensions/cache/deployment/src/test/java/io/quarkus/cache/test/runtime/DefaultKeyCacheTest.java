package io.quarkus.cache.test.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheResult;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests a cache with a <b>default</b> cache key.
 */
public class DefaultKeyCacheTest {

    private static final Object KEY = new Object();

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class).addClass(CachedService.class));

    @Inject
    CachedService cachedService;

    @Test
    public void testAllCacheAnnotations() {
        assertEquals(0, cachedService.getCacheResultInvocations());

        // STEP 1
        // Let's start by getting something and cache the result by calling a no-arg @CacheResult-annotated method.
        String value1 = cachedService.cacheResult();
        assertEquals(1, cachedService.getCacheResultInvocations());

        // STEP 2
        // If we get it again, the @CacheResult-annotated method should not be invoked and the result should come from the cache.
        // Since it comes from the cache, the object reference should be equal to STEP 1.
        String value2 = cachedService.cacheResult();
        assertEquals(1, cachedService.getCacheResultInvocations());
        assertTrue(value1 == value2);

        // STEP 3
        // Let's add an entry with a user-provided cache key to make sure it is not deleted by STEP 4.
        String value3 = cachedService.cacheResultWithKey(KEY);
        assertEquals(2, cachedService.getCacheResultInvocations());

        // STEP 4
        // Now, we want to delete the STEP 2 entry from the cache, but keep the STEP 3 entry.
        cachedService.invalidate();

        // STEP 5
        // If we try to get the STEP 2 entry, the @CacheResult-annotated method should be invoked and return a new object.
        String value5 = cachedService.cacheResult();
        assertEquals(3, cachedService.getCacheResultInvocations());
        assertTrue(value2 != value5);

        // STEP 6
        // But if we try to get the STEP 3 entry, it should come from the cache with no method invocation.
        String value6 = cachedService.cacheResultWithKey(KEY);
        assertEquals(3, cachedService.getCacheResultInvocations());
        assertTrue(value3 == value6);

        // STEP 7
        // Almost done, let's clear the entire cache.
        cachedService.invalidateAll();

        // STEP 8
        // If we try to get the STEP 5 entry, the @CacheResult-annotated method should be invoked and return a new object.
        String value8 = cachedService.cacheResult();
        assertEquals(4, cachedService.getCacheResultInvocations());
        assertTrue(value5 != value8);

        // STEP 9
        // If we try to get the STEP 6 entry, the @CacheResult-annotated method should be invoked and return a new object.
        String value9 = cachedService.cacheResultWithKey(KEY);
        assertEquals(5, cachedService.getCacheResultInvocations());
        assertTrue(value6 != value9);
    }

    @Singleton
    static class CachedService {

        private static final String CACHE_NAME = "defaultKeyCache";

        private int cacheResultInvocations;

        @CacheResult(cacheName = CACHE_NAME)
        public String cacheResult() {
            cacheResultInvocations++;
            return new String();
        }

        @CacheResult(cacheName = CACHE_NAME)
        public String cacheResultWithKey(Object key) {
            cacheResultInvocations++;
            return new String();
        }

        @CacheInvalidate(cacheName = CACHE_NAME)
        public void invalidate() {
        }

        @CacheInvalidateAll(cacheName = CACHE_NAME)
        public void invalidateAll() {
        }

        public int getCacheResultInvocations() {
            return cacheResultInvocations;
        }
    }
}
