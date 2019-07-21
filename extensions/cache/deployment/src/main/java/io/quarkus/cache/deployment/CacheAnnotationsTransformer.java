package io.quarkus.cache.deployment;

import static io.quarkus.cache.deployment.CacheDeploymentConstants.AUGMENTED_CACHE_INVALIDATE;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.AUGMENTED_CACHE_INVALIDATE_ALL;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.AUGMENTED_CACHE_RESULT;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE_ALL;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_NAME_PARAMETER_NAME;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_RESULT;

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

    @Override
    public boolean appliesTo(Kind kind) {
        return Kind.METHOD == kind;
    }

    @Override
    public void transform(TransformationContext context) {
        MethodInfo method = context.getTarget().asMethod();
        augmentAnnotationIfPresent(context, method, CACHE_RESULT, AUGMENTED_CACHE_RESULT);
        augmentAnnotationIfPresent(context, method, CACHE_INVALIDATE, AUGMENTED_CACHE_INVALIDATE);
        augmentAnnotationIfPresent(context, method, CACHE_INVALIDATE_ALL, AUGMENTED_CACHE_INVALIDATE_ALL);
    }

    private void augmentAnnotationIfPresent(TransformationContext context, MethodInfo method, DotName before, DotName after) {
        if (method.hasAnnotation(before)) {
            String cacheName = method.annotation(before).value(CACHE_NAME_PARAMETER_NAME).asString();
            AnnotationValue annotationValue = AnnotationValue.createStringValue(CACHE_NAME_PARAMETER_NAME, cacheName);
            context.transform().add(after, annotationValue).done();
        }
    }
}
