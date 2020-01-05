package io.quarkus.cache.deployment;

import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE_ALL;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_NAME_PARAMETER_NAME;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_OPERATION;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_RESULT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

import io.quarkus.arc.processor.AnnotationsTransformer;

/**
 * This class transforms at build time the caching API annotations (which have a mandatory {@code cacheName} parameter and can
 * only be used on methods) into augmented annotations that are used as interceptors bindings for the caching interceptors.
 */
public class CacheAnnotationsTransformer implements AnnotationsTransformer {

    private static final String INVALIDATE_ALL_PARAMETER_NAME = "invalidateAll";
    private static final String INVALIDATE_PARAMETER_NAME = "invalidate";
    private static final String LOAD_PARAMETER_NAME = "load";
    private static final String LOCK_TIMEOUT_PARAMETER_NAME = "lockTimeout";

    @Override
    public boolean appliesTo(Kind kind) {
        return Kind.METHOD == kind;
    }

    @Override
    public void transform(TransformationContext context) {
        MethodInfo method = context.getTarget().asMethod();
        AnnotationInstance cacheInvalidateAll = method.annotation(CACHE_INVALIDATE_ALL);
        AnnotationInstance cacheInvalidate = method.annotation(CACHE_INVALIDATE);
        AnnotationInstance cacheResult = method.annotation(CACHE_RESULT);
        if (cacheInvalidateAll != null || cacheInvalidate != null || cacheResult != null) {
            List<AnnotationValue> parameters = new ArrayList<>();
            parameters.add(findCacheName(cacheInvalidateAll, cacheInvalidate, cacheResult));
            if (cacheInvalidateAll != null) {
                parameters.add(AnnotationValue.createBooleanValue(INVALIDATE_ALL_PARAMETER_NAME, true));
            }
            if (cacheInvalidate != null) {
                parameters.add(AnnotationValue.createBooleanValue(INVALIDATE_PARAMETER_NAME, true));
            }
            if (cacheResult != null) {
                parameters.add(AnnotationValue.createBooleanValue(LOAD_PARAMETER_NAME, true));
                findLockTimeout(method, CACHE_RESULT).ifPresent(parameters::add);
            }
            context.transform().add(CACHE_OPERATION, parameters.toArray(new AnnotationValue[parameters.size()])).done();
        }
    }

    private AnnotationValue findCacheName(AnnotationInstance... annotations) {
        if (annotations != null) {
            for (AnnotationInstance annotation : annotations) {
                if (annotation != null) {
                    String cacheName = annotation.value(CACHE_NAME_PARAMETER_NAME).asString();
                    return AnnotationValue.createStringValue(CACHE_NAME_PARAMETER_NAME, cacheName);
                }
            }
        }
        // The following exception should never be thrown.
        throw new IllegalStateException("Unable to find the cache name");
    }

    private Optional<AnnotationValue> findLockTimeout(MethodInfo method, DotName apiAnnotation) {
        AnnotationValue lockTimeout = method.annotation(apiAnnotation).value(LOCK_TIMEOUT_PARAMETER_NAME);
        if (lockTimeout == null) {
            return Optional.empty();
        }
        return Optional.of(AnnotationValue.createLongalue(LOCK_TIMEOUT_PARAMETER_NAME, lockTimeout.asLong()));
    }
}
