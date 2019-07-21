package io.quarkus.cache.deployment;

import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE_ALL;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_RESULT;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.NON_BLOCKING_RETURN_TYPES;

import java.util.List;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.quarkus.arc.processor.AnnotationStore;
import io.quarkus.arc.processor.BeanInfo;
import io.quarkus.cache.deployment.exception.MultipleCacheAnnotationsException;
import io.quarkus.cache.deployment.exception.NonBlockingReturnTypeException;
import io.quarkus.cache.deployment.exception.VoidReturnTypeException;

public class CacheMethodValidator {

    public static void validateAnnotations(AnnotationStore annotationStore, BeanInfo bean, MethodInfo method,
            List<Throwable> throwables) {

        int methodAnnotationsCount = 0;
        if (validateCacheResultAnnotation(annotationStore, bean, method, throwables)) {
            methodAnnotationsCount++;
        }
        if (validateCacheInvalidateAnnotation(annotationStore, bean, method, throwables)) {
            methodAnnotationsCount++;
        }
        if (validateCacheInvalidateAllAnnotation(annotationStore, bean, method, throwables)) {
            methodAnnotationsCount++;
        }
        if (methodAnnotationsCount > 1) {
            String error = "Multiple incompatible cache annotations found on the same method";
            throwables.add(new MultipleCacheAnnotationsException(buildExceptionMessage(error, bean.getBeanClass(), method)));
        }
    }

    private static boolean validateCacheAnnotation(AnnotationStore annotationStore, MethodInfo method,
            DotName annotationDotName, Runnable validationChecks) {
        boolean found = false;
        AnnotationInstance annotation = annotationStore.getAnnotation(method, annotationDotName);
        if (annotation != null) {
            found = true;
            validationChecks.run();
        }
        return found;
    }

    private static boolean validateCacheResultAnnotation(AnnotationStore annotationStore, BeanInfo bean, MethodInfo method,
            List<Throwable> throwables) {
        return validateCacheAnnotation(annotationStore, method, CACHE_RESULT, () -> {
            checkVoidReturnType(CACHE_RESULT, bean, method).ifPresent(throwables::add);
            checkNonBlockingReturnType(CACHE_RESULT, bean, method).ifPresent(throwables::add);
        });
    }

    private static boolean validateCacheInvalidateAnnotation(AnnotationStore annotationStore, BeanInfo bean, MethodInfo method,
            List<Throwable> throwables) {
        return validateCacheAnnotation(annotationStore, method, CACHE_INVALIDATE, () -> {
            // No validation check for @CacheInvalidate for now. Replace this comment with a check if you need to add one.
        });
    }

    private static boolean validateCacheInvalidateAllAnnotation(AnnotationStore annotationStore, BeanInfo bean,
            MethodInfo method, List<Throwable> throwables) {
        return validateCacheAnnotation(annotationStore, method, CACHE_INVALIDATE_ALL, () -> {
            // No validation check for @CacheInvalidateAll for now. Replace this comment with a check if you need to add one.
        });
    }

    private static Optional<VoidReturnTypeException> checkVoidReturnType(DotName annotation, BeanInfo bean, MethodInfo method) {
        if (method.returnType().kind() == Type.Kind.VOID) {
            String error = annotation + " annotation is not allowed on a method returning void";
            return Optional.of(new VoidReturnTypeException(buildExceptionMessage(error, bean.getBeanClass(), method)));
        }
        return Optional.empty();
    }

    private static Optional<NonBlockingReturnTypeException> checkNonBlockingReturnType(DotName annotation, BeanInfo bean,
            MethodInfo method) {
        if (NON_BLOCKING_RETURN_TYPES.contains(method.returnType().name())) {
            String error = annotation + " annotation on non-blocking methods is not supported yet";
            return Optional.of(new NonBlockingReturnTypeException(buildExceptionMessage(error, bean.getBeanClass(), method)));
        }
        return Optional.empty();
    }

    private static String buildExceptionMessage(String error, DotName beanClass, MethodInfo method) {
        return error + ": [class: " + beanClass + ", method: " + method + "]";
    }
}
