package com.supplywise.supplywise.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CognitoTokenValidationExceptionTest {

    @Test
    void testConstructorWithMessage() {
        // Given
        String errorMessage = "Invalid token format";

        // When
        CognitoTokenValidationException exception = new CognitoTokenValidationException(errorMessage);

        // Then
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        // Given
        String errorMessage = "Failed to validate token";
        IllegalArgumentException cause = new IllegalArgumentException("Token expired");

        // When
        CognitoTokenValidationException exception = new CognitoTokenValidationException(errorMessage, cause);

        // Then
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Token expired", exception.getCause().getMessage());
    }

    @Test
    void testExceptionInheritance() {
        // Given
        CognitoTokenValidationException exception = new CognitoTokenValidationException("Test message");

        // Then
        assertTrue(exception instanceof RuntimeException);
    }
}