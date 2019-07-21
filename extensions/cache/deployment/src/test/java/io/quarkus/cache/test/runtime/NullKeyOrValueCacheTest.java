package io.quarkus.cache.test.runtime;

import static io.quarkus.cache.runtime.CacheKeyBuilder.NULL_KEYS_NOT_SUPPORTED_MSG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests {@code null} cache keys or values.
 */
public class NullKeyOrValueCacheTest {

    private static final Object KEY = new Object();

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class).addClass(CachedService.class));

    @Inject
    CachedService cachedService;

    @Test
    public void testNullKeys() {
        assertThrows(NullPointerException.class, () -> {
            cachedService.nullKeyCacheResult(null);
        }, NULL_KEYS_NOT_SUPPORTED_MSG);
        assertThrows(NullPointerException.class, () -> {
            cachedService.nullKeyInvalidate(null);
        }, NULL_KEYS_NOT_SUPPORTED_MSG);
    }

    @Test
    public void testNullValues() {
        assertEquals(0, cachedService.getNullValueCacheResultInvocations());

        // Let's start by getting a null value and cache it by calling a @CacheResult-annotated method.   
        Object value1 = cachedService.nullValueCacheResult(KEY);
        assertEquals(1, cachedService.getNullValueCacheResultInvocations());
        assertNull(value1);

        // If we get it again, the @CacheResult-annotated method should not be invoked and the result should come from the cache. 
        Object value2 = cachedService.nullValueCacheResult(KEY);
        assertEquals(1, cachedService.getNullValueCacheResultInvocations());
        assertNull(value2);
    }

    @Dependent
    static class CachedService {

        private int nullValueCacheResultInvocations;

        @CacheResult(cacheName = "nullKeyCache")
        public Object nullKeyCacheResult(Object key) {
            return null;
        }

        @CacheInvalidate(cacheName = "nullKeyCache")
        public void nullKeyInvalidate(Object key) {
        }

        @CacheResult(cacheName = "nullValueCache")
        public Object nullValueCacheResult(Object key) {
            nullValueCacheResultInvocations++;
            return null;
        }

        public int getNullValueCacheResultInvocations() {
            return nullValueCacheResultInvocations;
        }
    }
}
