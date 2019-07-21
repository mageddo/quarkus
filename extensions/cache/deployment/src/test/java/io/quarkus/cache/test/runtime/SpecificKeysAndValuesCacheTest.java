package io.quarkus.cache.test.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.cache.CacheLoad;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests specific cache keys and values.
 */
public class SpecificKeysAndValuesCacheTest {

    private static final Object KEY = new Object();

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class).addClass(CachedService.class));

    @Inject
    CachedService cachedService;

    @Test
    public void testNullKey() {
        assertEquals(0, cachedService.getNullKeyInvocations());

        // STEP 1
        // Let's start by getting something using a null key and cache the result by calling a @CacheLoad-annotated method.
        String value1 = cachedService.nullKey(null);
        assertEquals(1, cachedService.getNullKeyInvocations());

        // STEP 2
        // If we get it again, the @CacheLoad-annotated method should not be invoked and the result should come from the cache.
        // Since it comes from the cache, the object reference should be equal to STEP 1.
        String value2 = cachedService.nullKey(null);
        assertEquals(1, cachedService.getNullKeyInvocations());
        assertTrue(value1 == value2);
    }

    @Test
    public void testNullValue() {
        assertEquals(0, cachedService.getNullValueInvocations());

        // Let's start by getting a null value and cache it by calling a @CacheLoad-annotated method.
        Object value1 = cachedService.nullValue(KEY);
        assertEquals(1, cachedService.getNullValueInvocations());
        assertNull(value1);

        // If we get it again, the @CacheLoad-annotated method should not be invoked and the result should come from the cache.
        Object value2 = cachedService.nullValue(KEY);
        assertEquals(1, cachedService.getNullValueInvocations());
        assertNull(value2);
    }

    @Test
    public void testPrimitiveValue() {
        assertEquals(0, cachedService.getPrimitiveValueInvocations());

        // Let's start by getting a primitive value and cache it by calling a @CacheLoad-annotated method.
        long value1 = cachedService.primitiveValue(KEY);
        assertEquals(1, cachedService.getPrimitiveValueInvocations());

        // If we get it again, the @CacheLoad-annotated method should not be invoked and the result should come from the cache.
        long value2 = cachedService.primitiveValue(KEY);
        assertEquals(1, cachedService.getPrimitiveValueInvocations());
        assertTrue(value1 == value2);
    }

    @Dependent
    static class CachedService {

        private int nullKeyInvocations;
        private int nullValueInvocations;
        private int primitiveValueInvocations;

        @CacheLoad(cacheName = "nullKeyCache")
        public String nullKey(Object key) {
            nullKeyInvocations++;
            return new String();
        }

        @CacheLoad(cacheName = "nullValueCache")
        public Object nullValue(Object key) {
            nullValueInvocations++;
            return null;
        }

        @CacheLoad(cacheName = "primitiveValueCache")
        public long primitiveValue(Object key) {
            primitiveValueInvocations++;
            return 123L;
        }

        public int getNullKeyInvocations() {
            return nullKeyInvocations;
        }

        public int getNullValueInvocations() {
            return nullValueInvocations;
        }

        public int getPrimitiveValueInvocations() {
            return primitiveValueInvocations;
        }
    }
}
