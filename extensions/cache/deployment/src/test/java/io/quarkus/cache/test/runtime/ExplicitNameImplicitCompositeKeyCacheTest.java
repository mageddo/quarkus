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
import io.quarkus.cache.CacheLoad;
import io.quarkus.cache.CacheStore;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests a cache with an <b>explicit</b> name and <b>implicit composite</b> cache keys.
 */
public class ExplicitNameImplicitCompositeKeyCacheTest {

    private static final String KEY_1_ELEMENT_1 = "foo";
    private static final int KEY_1_ELEMENT_2 = 123;
    private static final String KEY_2_ELEMENT_1 = "bar";
    private static final int KEY_2_ELEMENT_2 = 456;

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
        String value1 = cachedService.load(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2);
        assertEquals(1, cachedService.getLoadInvocations());

        // STEP 2
        // If we get it again, the @CacheLoad-annotated method should not be invoked and the result should come from the cache.
        // Since it comes from the cache, the object reference should be equal to STEP 1.
        String value2 = cachedService.load(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2);
        assertEquals(1, cachedService.getLoadInvocations());
        assertTrue(value1 == value2);

        // STEP 3
        // Now, let's call the same method with the same first argument and a different second argument.
        // We changed one element of the key, so the @CacheLoad-annotated method should be invoked and return a new object.
        String value3 = cachedService.load(KEY_1_ELEMENT_1, 789);
        assertEquals(2, cachedService.getLoadInvocations());
        assertTrue(value1 != value3);

        // STEP 4
        // We need to apply STEP 3 logic to the other argument, it should produce the same result.
        String value4 = cachedService.load("quarkus", KEY_1_ELEMENT_2);
        assertEquals(3, cachedService.getLoadInvocations());
        assertTrue(value1 != value4);

        // STEP 5
        // If the entire key is changed, another result should be returned from the @CacheLoad-annotated method invocation.
        String value5 = cachedService.load(KEY_2_ELEMENT_1, KEY_2_ELEMENT_2);
        assertEquals(4, cachedService.getLoadInvocations());
        assertTrue(value2 != value5);

        // STEP 6
        // Now, let's try to force a reload of the STEP 1 result using the same key.
        // Objects references should be different since the @CacheStore-annotated method was invoked and returned a new
        // instance.
        String value6 = cachedService.store(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2);
        assertEquals(1, cachedService.getStoreInvocations());
        assertTrue(value1 != value6);

        // STEP 7
        // If we do it again, it should invoke the @CacheStore-annotated method and return another new instance.
        String value7 = cachedService.store(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2);
        assertEquals(2, cachedService.getStoreInvocations());
        assertTrue(value6 != value7);

        // STEP 8
        // If we try to get the STEP 7 object from the cache, the @CacheLoad-annotated method should not be invoked.
        // The object reference should also be identical between STEPs 7 and 8.
        String value8 = cachedService.load(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2);
        assertEquals(4, cachedService.getLoadInvocations());
        assertTrue(value7 == value8);

        // STEP 9
        // Now, we want to delete the STEP 7 entry from the cache, but keep the STEP 5 entry.
        cachedService.invalidate(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2);

        // STEP 10
        // If we try to get the STEP 7 entry, the @CacheLoad-annotated method should be invoked and return a new object.
        String value10 = cachedService.load(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2);
        assertEquals(5, cachedService.getLoadInvocations());
        assertTrue(value7 != value10);

        // STEP 11
        // But if we try to get the STEP 5 entry, it should come from the cache with no method invocation.
        String value11 = cachedService.load(KEY_2_ELEMENT_1, KEY_2_ELEMENT_2);
        assertEquals(5, cachedService.getLoadInvocations());
        assertTrue(value5 == value11);

        // STEP 12
        // Almost done, let's clear the entire cache.
        cachedService.invalidateAll();

        // STEP 13
        // If we try to get the STEP 10 entry, the @CacheLoad-annotated method should be invoked and return a new object.
        String value13 = cachedService.load(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2);
        assertEquals(6, cachedService.getLoadInvocations());
        assertTrue(value10 != value13);

        // STEP 14
        // If we try to get the STEP 11 entry, the @CacheLoad-annotated method should be invoked and return a new object.
        String value14 = cachedService.load(KEY_2_ELEMENT_1, KEY_2_ELEMENT_2);
        assertEquals(7, cachedService.getLoadInvocations());
        assertTrue(value11 != value14);
    }

    @Singleton
    static class CachedService {

        public static final String CACHE_NAME = "explicitNameImplicitCompositeKeyCachedServiceCache";

        private int loadInvocations;
        private int storeInvocations;

        @CacheLoad(cacheName = CACHE_NAME)
        public String load(String keyElement1, int keyElement2) {
            loadInvocations++;
            return new String();
        }

        @CacheStore(cacheName = CACHE_NAME)
        public String store(String keyElement1, int keyElement2) {
            storeInvocations++;
            return new String();
        }

        @CacheInvalidate(cacheName = CACHE_NAME)
        public void invalidate(String keyElement1, int keyElement2) {
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
