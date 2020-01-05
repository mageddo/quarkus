package io.quarkus.cache.deployment;

import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE_ALL;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_NAME_PARAMETER_NAME;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_RESULT;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.quarkus.arc.processor.AnnotationStore;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.cache.deployment.exception.IllegalReturnTypeException;
import io.quarkus.cache.deployment.exception.MultipleCacheNamesException;

public class CacheMethodValidator {

    public static void validateAnnotations(AnnotationStore annotationStore, BeanInfo bean, MethodInfo method,
            List<Throwable> throwables) {
        AnnotationInstance cacheInvalidateAll = annotationStore.getAnnotation(method, CACHE_INVALIDATE_ALL);
        AnnotationInstance cacheInvalidate = annotationStore.getAnnotation(method, CACHE_INVALIDATE);
        AnnotationInstance cacheResult = annotationStore.getAnnotation(method, CACHE_RESULT);
        checkCacheNames(bean, method, cacheInvalidateAll, cacheInvalidate, cacheResult).ifPresent(throwables::add);
        if (cacheResult != null) {
            checkVoidReturnType(CACHE_RESULT, bean, method).ifPresent(throwables::add);
        }
    }

    private static Optional<Exception> checkCacheNames(BeanInfo bean, MethodInfo method, AnnotationInstance... annotations) {
        if (annotations != null) {
            String knownCacheName = null;
            for (AnnotationInstance annotation : annotations) {
                if (annotation != null) {
                    String newCacheName = annotation.value(CACHE_NAME_PARAMETER_NAME).asString();
                    if (knownCacheName != null && !newCacheName.equals(knownCacheName)) {
                        String error = "All caching annotations used on a single method must share the same cache name";
                        return Optional
                                .of(new MultipleCacheNamesException(buildExceptionMessage(error, bean.getBeanClass(), method)));
                    }
                    knownCacheName = newCacheName;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<IllegalReturnTypeException> checkVoidReturnType(DotName annotation, BeanInfo bean,
            MethodInfo method) {
        if (method.returnType().kind() == Type.Kind.VOID) {
            String error = annotation + " annotation is not allowed on a method returning void";
            return Optional.of(new IllegalReturnTypeException(buildExceptionMessage(error, bean.getBeanClass(), method)));
        }
        return Optional.empty();
    }

    private static String buildExceptionMessage(String error, DotName beanClass, MethodInfo method) {
        return error + ": [class: " + beanClass + ", method: " + method + "]";
    }
}
