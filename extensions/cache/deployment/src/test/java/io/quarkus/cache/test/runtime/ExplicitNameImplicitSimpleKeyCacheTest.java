package io.quarkus.cache.test.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.cache.CacheLoad;
import io.quarkus.cache.CacheStore;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests a cache with an <b>explicit</b> name and <b>implicit simple</b> cache keys.
 */
public class ExplicitNameImplicitSimpleKeyCacheTest {

    private static final Object KEY_1 = new Object();
    private static final Object KEY_2 = new Object();

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class).addClass(CachedService.class));

    @Inject
    CachedService cachedService;

    @Test
    public void testAllCacheAnnotations() {
        assertEquals(0, cachedService.getLoadInvocations());
        assertEquals(0, cachedService.getStoreInvocations());

        // STEP 1
        // Let's start by getting something and cache the result by calling a @CacheLoad-annotated method.
        String value1 = cachedService.load(KEY_1);
        assertEquals(1, cachedService.getLoadInvocations());

        // STEP 2
        // If we get it again, the @CacheLoad-annotated method should not be invoked and the result should come from the cache.
        // Since it comes from the cache, the object reference should be equal to STEP 1.
        String value2 = cachedService.load(KEY_1);
        assertEquals(1, cachedService.getLoadInvocations());
        assertTrue(value1 == value2);

        // STEP 3
        // If the key is changed, another result should be returned from the @CacheLoad-annotated method invocation.
        String value3 = cachedService.load(KEY_2);
        assertEquals(2, cachedService.getLoadInvocations());
        assertTrue(value2 != value3);

        // STEP 4
        // Now, let's try to force a reload of the STEP 1 result using the same key.
        // Objects references should be different since the @CacheStore-annotated method was invoked and returned a new
        // instance.
        String value4 = cachedService.store(KEY_1);
        assertEquals(1, cachedService.getStoreInvocations());
        assertTrue(value1 != value4);

        // STEP 5
        // If we do it again, it should invoke the @CacheStore-annotated method and return another new instance.
        String value5 = cachedService.store(KEY_1);
        assertEquals(2, cachedService.getStoreInvocations());
        assertTrue(value4 != value5);

        // STEP 6
        // If we try to get the STEP 5 object from the cache, the @CacheLoad-annotated method should not be invoked.
        // The object reference should also be identical between STEPs 5 and 6.
        String value6 = cachedService.load(KEY_1);
        assertEquals(2, cachedService.getLoadInvocations());
        assertTrue(value5 == value6);

        // STEP 7
        // Now, we want to delete the STEP 5 entry from the cache, but keep the STEP 3 entry.
        cachedService.invalidate(KEY_1);

        // STEP 8
        // If we try to get the STEP 5 entry, the @CacheLoad-annotated method should be invoked and return a new object.
        String value8 = cachedService.load(KEY_1);
        assertEquals(3, cachedService.getLoadInvocations());
        assertTrue(value5 != value8);

        // STEP 9
        // But if we try to get the STEP 3 entry, it should come from the cache with no method invocation.
        String value9 = cachedService.load(KEY_2);
        assertEquals(3, cachedService.getLoadInvocations());
        assertTrue(value3 == value9);

        // STEP 10
        // Almost done, let's clear the entire cache.
        cachedService.invalidateAll();

        // STEP 11
        // If we try to get the STEP 8 entry, the @CacheLoad-annotated method should be invoked and return a new object.
        String value11 = cachedService.load(KEY_1);
        assertEquals(4, cachedService.getLoadInvocations());
        assertTrue(value8 != value11);

        // STEP 12
        // If we try to get the STEP 9 entry, the @CacheLoad-annotated method should be invoked and return a new object.
        String value12 = cachedService.load(KEY_2);
        assertEquals(5, cachedService.getLoadInvocations());
        assertTrue(value9 != value12);
    }

    @ApplicationScoped
    static class CachedService {

        public static final String CACHE_NAME = "explicitNameImplicitSimpleKeyCachedServiceCache";

        private int loadInvocations;
        private int storeInvocations;

        @CacheLoad(cacheName = CACHE_NAME)
        public String load(Object key) {
            loadInvocations++;
            return new String();
        }

        @CacheStore(cacheName = CACHE_NAME)
        public String store(Object key) {
            storeInvocations++;
            return new String();
        }

        @CacheInvalidate(cacheName = CACHE_NAME)
        public void invalidate(Object key) {
        }

        @CacheInvalidateAll(cacheName = CACHE_NAME)
        public void invalidateAll() {
        }

        public int getLoadInvocations() {
            return loadInvocations;
        }

        public int getStoreInvocations() {
            return storeInvocations;
        }
    }
}
