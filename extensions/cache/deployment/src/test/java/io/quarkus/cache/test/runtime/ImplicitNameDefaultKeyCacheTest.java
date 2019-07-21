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
import io.quarkus.cache.CacheLoad;
import io.quarkus.cache.CacheStore;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests a cache with an <b>implicit</b> name and a <b>default</b> cache key.<br>
 */
public class ImplicitNameDefaultKeyCacheTest {

    private static final Object KEY = new Object();

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
        // Let's start by getting something and cache the result by calling a no-arg @CacheLoad-annotated method.
        String value1 = cachedService.load();
        assertEquals(1, cachedService.getLoadInvocations());

        // STEP 2
        // If we get it again, the @CacheLoad-annotated method should not be invoked and the result should come from the cache.
        // Since it comes from the cache, the object reference should be equal to STEP 1.
        String value2 = cachedService.load();
        assertEquals(1, cachedService.getLoadInvocations());
        assertTrue(value1 == value2);

        // STEP 3
        // Now, let's try to force a reload of the STEP 1 result.
        // Objects references should be different since the @CacheStore-annotated method was invoked and returned a new
        // instance.
        String value3 = cachedService.store();
        assertEquals(1, cachedService.getStoreInvocations());
        assertTrue(value1 != value3);

        // STEP 4
        // If we do it again, it should invoke the @CacheStore-annotated method and return another new instance.
        String value4 = cachedService.store();
        assertEquals(2, cachedService.getStoreInvocations());
        assertTrue(value3 != value4);

        // STEP 5
        // If we try to get the STEP 4 object from the cache, the @CacheLoad-annotated method should not be invoked.
        // The object reference should also be identical between STEPs 4 and 5.
        String value5 = cachedService.load();
        assertEquals(1, cachedService.getLoadInvocations());
        assertTrue(value4 == value5);

        // STEP 6
        // Let's add an entry with a user-provided cache key to make sure it is not deleted by STEP 7.
        String value6 = cachedService.loadWithKey(KEY);
        assertEquals(2, cachedService.getLoadInvocations());

        // STEP 7
        // Now, we want to delete the STEP 5 entry from the cache, but keep the STEP 6 entry.
        cachedService.invalidate();

        // STEP 8
        // If we try to get the STEP 5 entry, the @CacheLoad-annotated method should be invoked and return a new object.
        String value8 = cachedService.load();
        assertEquals(3, cachedService.getLoadInvocations());
        assertTrue(value5 != value8);

        // STEP 9
        // But if we try to get the STEP 6 entry, it should come from the cache with no method invocation.
        String value9 = cachedService.loadWithKey(KEY);
        assertEquals(3, cachedService.getLoadInvocations());
        assertTrue(value6 == value9);

        // STEP 10
        // Almost done, let's clear the entire cache.
        cachedService.invalidateAll();

        // STEP 11
        // If we try to get the STEP 8 entry, the @CacheLoad-annotated method should be invoked and return a new object.
        String value11 = cachedService.load();
        assertEquals(4, cachedService.getLoadInvocations());
        assertTrue(value8 != value11);

        // STEP 12
        // If we try to get the STEP 9 entry, the @CacheLoad-annotated method should be invoked and return a new object.
        String value12 = cachedService.loadWithKey(KEY);
        assertEquals(5, cachedService.getLoadInvocations());
        assertTrue(value9 != value12);
    }

    @Dependent
    static class CachedService {

        private int loadInvocations;
        private int storeInvocations;

        @CacheLoad
        public String load() {
            loadInvocations++;
            return new String();
        }

        @CacheLoad
        public String loadWithKey(Object key) {
            loadInvocations++;
            return new String();
        }

        @CacheStore
        public String store() {
            storeInvocations++;
            return new String();
        }

        @CacheInvalidate
        public void invalidate() {
        }

        @CacheInvalidateAll
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
