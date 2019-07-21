package io.quarkus.cache.deployment;

import static io.quarkus.cache.deployment.CacheDeploymentConstants.CACHE_NAME_PARAMETER_NAME;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

public class CacheNameResolver {

    public static String resolve(AnnotationInstance cacheAnnotation) {
        AnnotationValue explicitCacheName = cacheAnnotation.value(CACHE_NAME_PARAMETER_NAME);
        if (explicitCacheName == null) {
            return cacheAnnotation.target().asMethod().declaringClass().name().toString();
        } else {
            return explicitCacheName.asString();
        }
    }
}
