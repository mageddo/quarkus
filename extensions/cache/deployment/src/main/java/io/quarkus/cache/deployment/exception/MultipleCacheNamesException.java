package io.quarkus.cache.deployment.exception;

@SuppressWarnings("serial")
public class MultipleCacheNamesException extends RuntimeException {

    public MultipleCacheNamesException(String message) {
        super(message);
    }
}
