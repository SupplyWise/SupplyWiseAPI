package com.supplywise.supplywise.exception;

public class CognitoTokenValidationException extends RuntimeException {

    public CognitoTokenValidationException(String message) {
        super(message);
    }

    public CognitoTokenValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}