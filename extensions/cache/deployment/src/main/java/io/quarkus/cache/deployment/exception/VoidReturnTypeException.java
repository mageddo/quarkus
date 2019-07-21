package io.quarkus.cache.deployment.exception;

@SuppressWarnings("serial")
public class VoidReturnTypeException extends RuntimeException {

    public VoidReturnTypeException(String message) {
        super(message);
    }
}
