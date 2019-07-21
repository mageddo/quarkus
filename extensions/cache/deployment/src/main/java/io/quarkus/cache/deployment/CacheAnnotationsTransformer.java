package io.quarkus.cache.deployment;

import static io.quarkus.cache.deployment.CacheDeploymentConstants.AUGMENTED_CACHE_INVALIDATE;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.AUGMENTED_CACHE_INVALIDATE_ALL;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.AUGMENTED_CACHE_LOAD;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.AUGMENTED_CACHE_STORE;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_INVALIDATE_ALL;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_LOAD;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_NAME_PARAMETER_NAME;
import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_STORE;

import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;

import io.quarkus.arc.processor.AnnotationsTransformer;

/**
 * This class transforms at build time the user-added cache annotations (which may have an empty {@code cacheName} parameter)
 * into augmented cache annotations (which will always have a non-empty {@code cacheName} parameter based on
 * {@link CacheNameResolver} strategy). Cache interceptors will only intercept methods annotated with augmented annotations and
 * do nothing on user-added annotations.
 */
public class CacheAnnotationsTransformer implements AnnotationsTransformer {

    @Override
    public boolean appliesTo(Kind kind) {
        return Kind.METHOD == kind;
    }

    @Override
    public void transform(TransformationContext context) {
        MethodInfo method = context.getTarget().asMethod();
        augmentAnnotationIfPresent(context, method, CACHE_LOAD, AUGMENTED_CACHE_LOAD);
        augmentAnnotationIfPresent(context, method, CACHE_STORE, AUGMENTED_CACHE_STORE);
        augmentAnnotationIfPresent(context, method, CACHE_INVALIDATE, AUGMENTED_CACHE_INVALIDATE);
        augmentAnnotationIfPresent(context, method, CACHE_INVALIDATE_ALL, AUGMENTED_CACHE_INVALIDATE_ALL);
    }

    private void augmentAnnotationIfPresent(TransformationContext context, MethodInfo method, DotName before, DotName after) {
        if (method.hasAnnotation(before)) {
            String cacheName = CacheNameResolver.resolve(method.annotation(before));
            AnnotationValue annotationValue = AnnotationValue.createStringValue(CACHE_NAME_PARAMETER_NAME, cacheName);
            context.transform().add(after, annotationValue).done();
        }
    }
}
