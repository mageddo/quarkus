package io.quarkus.cache.test.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Locale;

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
 * Tests a cache with <b>explicit composite</b> cache keys.<br>
 * All methods with {@link CacheKey @CacheKey}-annotated arguments also have another argument which is not part of the key.
 */
public class ExplicitCompositeKeyCacheTest {

    private static final Locale KEY_1_ELEMENT_1 = Locale.US;
    private static final BigDecimal KEY_1_ELEMENT_2 = new BigDecimal(123);
    private static final Locale KEY_2_ELEMENT_1 = Locale.FRANCE;
    private static final BigDecimal KEY_2_ELEMENT_2 = new BigDecimal(456);

    @RegisterExtension
    static final QuarkusUnitTest TEST = new QuarkusUnitTest().setArchiveProducer(
            () -> ShrinkWrap.create(JavaArchive.class).addClass(CachedService.class));

    @Inject
    CachedService cachedService;

    @Test
    public void testAllCacheAnnotations() {
        assertEquals(0, cachedService.getCacheResultInvocations());

        // In most of the cached service methods calls below, a changing third argument will be passed to the methods.
        // The fact that it changes each time should not have any effect on the cache because it is not part of the cache key.

        // STEP 1
        // Let's start by getting something and cache the result by calling a @CacheResult-annotated method.
        String value1 = cachedService.cacheResult(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2, new Object());
        assertEquals(1, cachedService.getCacheResultInvocations());

        // STEP 2
        // If we get it again, the @CacheResult-annotated method should not be invoked and the result should come from the cache.
        // Since it comes from the cache, the object reference should be equal to STEP 1.
        String value2 = cachedService.cacheResult(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2, new Object());
        assertEquals(1, cachedService.getCacheResultInvocations());
        assertTrue(value1 == value2);

        // STEP 3
        // Now, let's call the same method with the same first argument and a different second argument.
        // We changed one element of the key, so the @CacheResult-annotated method should be invoked and return a new object.
        String value3 = cachedService.cacheResult(KEY_1_ELEMENT_1, new BigDecimal(789), new Object());
        assertEquals(2, cachedService.getCacheResultInvocations());
        assertTrue(value2 != value3);

        // STEP 4
        // We need to apply STEP 3 logic to the other argument, it should produce the same result.
        String value4 = cachedService.cacheResult(Locale.JAPAN, KEY_1_ELEMENT_2, new Object());
        assertEquals(3, cachedService.getCacheResultInvocations());
        assertTrue(value2 != value4);

        // STEP 5
        // If the entire key is changed, another result should be returned from the @CacheResult-annotated method invocation.
        String value5 = cachedService.cacheResult(KEY_2_ELEMENT_1, KEY_2_ELEMENT_2, new Object());
        assertEquals(4, cachedService.getCacheResultInvocations());
        assertTrue(value2 != value5);

        // STEP 6
        // Now, we want to delete the STEP 2 entry from the cache, but keep the STEP 5 entry.
        cachedService.invalidate(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2, new Object());

        // STEP 7
        // If we try to get the STEP 2 entry, the @CacheResult-annotated method should be invoked and return a new object.
        String value7 = cachedService.cacheResult(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2, new Object());
        assertEquals(5, cachedService.getCacheResultInvocations());
        assertTrue(value2 != value7);

        // STEP 8
        // But if we try to get the STEP 5 entry, it should come from the cache with no method invocation.
        String value8 = cachedService.cacheResult(KEY_2_ELEMENT_1, KEY_2_ELEMENT_2, new Object());
        assertEquals(5, cachedService.getCacheResultInvocations());
        assertTrue(value5 == value8);

        // STEP 9
        // Almost done, let's clear the entire cache.
        cachedService.invalidateAll();

        // STEP 10
        // If we try to get the STEP 7 entry, the @CacheResult-annotated method should be invoked and return a new object.
        String value10 = cachedService.cacheResult(KEY_1_ELEMENT_1, KEY_1_ELEMENT_2, new Object());
        assertEquals(6, cachedService.getCacheResultInvocations());
        assertTrue(value7 != value10);

        // STEP 11
        // If we try to get the STEP 8 entry, the @CacheResult-annotated method should be invoked and return a new object.
        String value11 = cachedService.cacheResult(KEY_2_ELEMENT_1, KEY_2_ELEMENT_2, new Object());
        assertEquals(7, cachedService.getCacheResultInvocations());
        assertTrue(value8 != value11);
    }

    @Dependent
    static class CachedService {

        private static final String CACHE_NAME = "explicitCompositeKeyCache";

        private int cacheResultInvocations;

        @CacheResult(cacheName = CACHE_NAME)
        public String cacheResult(@CacheKey Locale keyElement1, @CacheKey BigDecimal keyElement2, Object notPartOfTheKey) {
            cacheResultInvocations++;
            return new String();
        }

        @CacheInvalidate(cacheName = CACHE_NAME)
        public void invalidate(@CacheKey Locale keyElement1, @CacheKey BigDecimal keyElement2, Object notPartOfTheKey) {
        }

        @CacheInvalidateAll(cacheName = CACHE_NAME)
        public void invalidateAll() {
        }

        public int getCacheResultInvocations() {
            return cacheResultInvocations;
        }
    }
}
