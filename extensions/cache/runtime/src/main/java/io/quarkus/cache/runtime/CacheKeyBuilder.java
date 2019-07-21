package io.quarkus.cache.runtime;

import java.util.Arrays;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CacheKeyBuilder {

    public Object build(Object... keyElements) {
        if (keyElements == null || keyElements.length == 0) {
            throw new IllegalArgumentException("At least one key element is required to build a cache key");
        } else if (keyElements.length == 1) {
            return keyElements[0];
        } else {
            return new CompositeCacheKey(keyElements);
        }
    }

    private static class CompositeCacheKey {

        private final Object[] keyElements;

        public CompositeCacheKey(Object... keyElements) {
            this.keyElements = Arrays.copyOf(keyElements, keyElements.length);
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(keyElements);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (CompositeCacheKey.class.isInstance(obj)) {
                final CompositeCacheKey other = (CompositeCacheKey) obj;
                return Arrays.deepEquals(keyElements, other.keyElements);
            }
            return false;
        }
    }
}
