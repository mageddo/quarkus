package io.quarkus.cache.test.deployment;

import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_NAME_PARAMETER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;
import org.junit.jupiter.api.Test;

import io.quarkus.cache.deployment.CacheNameResolver;
import io.quarkus.cache.runtime.augmented.AugmentedCacheLoad;

public class CacheNameResolverTest {

    @Test
    public void testExplicitCacheNameResolution() {
        String cacheName = "explicitCacheName";
        AnnotationInstance mockCacheAnnotation = createMockCacheAnnotation(getClass(), cacheName);
        String resolvedCacheName = CacheNameResolver.resolve(mockCacheAnnotation);
        assertEquals(cacheName, resolvedCacheName);
    }

    @Test
    public void testDefaultCacheNameResolution() {
        AnnotationInstance mockCacheAnnotation = createMockCacheAnnotation(getClass());
        String resolvedCacheName = CacheNameResolver.resolve(mockCacheAnnotation);
        assertEquals(getClass().getName(), resolvedCacheName);
    }

    private AnnotationInstance createMockCacheAnnotation(Class<?> declaringClass) {
        return createMockCacheAnnotation(declaringClass, null);
    }

    private AnnotationInstance createMockCacheAnnotation(Class<?> declaringClass, String cacheName) {
        DotName declaringClassName = DotName.createSimple(declaringClass.getName());
        @SuppressWarnings("deprecation")
        ClassInfo clazz = ClassInfo.create(declaringClassName, null, (short) 0, new DotName[0], Collections.emptyMap(), true);
        Type returnType = Type.create(DotName.createSimple(String.class.getName()), Kind.CLASS);
        MethodInfo method = MethodInfo.create(clazz, "mockMethod", new Type[0], returnType, (short) 0);
        DotName annotationName = DotName.createSimple(AugmentedCacheLoad.class.getName());
        return AnnotationInstance.create(annotationName, method, createMockCacheAnnotationValues(cacheName));
    }

    private List<AnnotationValue> createMockCacheAnnotationValues(String cacheName) {
        if (cacheName == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(AnnotationValue.createStringValue(CACHE_NAME_PARAMETER_NAME, cacheName));
        }
    }
}
