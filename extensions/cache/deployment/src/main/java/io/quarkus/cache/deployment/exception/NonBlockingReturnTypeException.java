package io.quarkus.cache.deployment.exception;

@SuppressWarnings("serial")
public class NonBlockingReturnTypeException extends RuntimeException {

    public NonBlockingReturnTypeException(String message) {
        super(message);
    }
}
