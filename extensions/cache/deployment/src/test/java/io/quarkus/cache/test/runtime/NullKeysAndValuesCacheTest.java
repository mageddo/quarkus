package io.quarkus.cache.test.runtime;

import static io.quarkus.cache.runtime.CacheKeyBuilder.NULL_KEYS_NOT_SUPPORTED_MSG;
import static io.quarkus.cache.runtime.augmented.AugmentedCacheAnnotationInterceptor.NULL_VALUES_NOT_SUPPORTED_MSG;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheLoad;
import io.quarkus.cache.CacheStore;
import io.quarkus.test.QuarkusUnitTest;

/**
 * Tests {@code null} cache keys and values.
 */
public class NullKeysAndValuesCacheTest {

    private static final Object KEY = new Object();

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class).addClass(CachedService.class));

    @Inject
    CachedService cachedService;

    @Test
    public void testNullKeys() {
        assertThrows(NullPointerException.class, () -> {
            cachedService.load(null);
        }, NULL_KEYS_NOT_SUPPORTED_MSG);
        assertThrows(NullPointerException.class, () -> {
            cachedService.store(null);
        }, NULL_KEYS_NOT_SUPPORTED_MSG);
        assertThrows(NullPointerException.class, () -> {
            cachedService.invalidate(null);
        }, NULL_KEYS_NOT_SUPPORTED_MSG);
    }

    @Test
    public void testNullValues() {
        assertThrows(NullPointerException.class, () -> {
            cachedService.load(KEY);
        }, NULL_VALUES_NOT_SUPPORTED_MSG);
        assertThrows(NullPointerException.class, () -> {
            cachedService.store(KEY);
        }, NULL_VALUES_NOT_SUPPORTED_MSG);
    }

    @ApplicationScoped
    static class CachedService {

        @CacheLoad
        public Object load(Object key) {
            return null;
        }

        @CacheStore
        public Object store(Object key) {
            return null;
        }

        @CacheInvalidate
        public void invalidate(Object key) {
        }
    }
}
