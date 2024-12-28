package com.supplywise.supplywise.config;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.supplywise.supplywise.exception.CognitoTokenValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CognitoTokenValidatorTest {

    private static final String VALID_TOKEN = "valid.test.token";

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        try (MockedStatic<SignedJWT> mockedStatic = mockStatic(SignedJWT.class)) {
            // Arrange
            SignedJWT mockSignedJWT = mock(SignedJWT.class);
            mockedStatic.when(() -> SignedJWT.parse(VALID_TOKEN)).thenReturn(mockSignedJWT);

            // Act
            boolean result = CognitoTokenValidator.validateToken(VALID_TOKEN);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    void validateToken_ParseException_ReturnsFalse() {
        try (MockedStatic<SignedJWT> mockedStatic = mockStatic(SignedJWT.class)) {
            // Arrange
            mockedStatic.when(() -> SignedJWT.parse(VALID_TOKEN))
                    .thenThrow(new ParseException("Invalid token", 0));

            // Act
            boolean result = CognitoTokenValidator.validateToken(VALID_TOKEN);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    void getClaims_ValidToken_ReturnsClaimsSet() throws Exception {
        try (MockedStatic<SignedJWT> mockedStatic = mockStatic(SignedJWT.class)) {
            // Arrange
            SignedJWT mockSignedJWT = mock(SignedJWT.class);
            JWTClaimsSet expectedClaims = new JWTClaimsSet.Builder()
                    .subject("testUser")
                    .build();
            when(mockSignedJWT.getJWTClaimsSet()).thenReturn(expectedClaims);
            mockedStatic.when(() -> SignedJWT.parse(VALID_TOKEN)).thenReturn(mockSignedJWT);

            // Act
            JWTClaimsSet result = CognitoTokenValidator.getClaims(VALID_TOKEN);

            // Assert
            assertEquals(expectedClaims, result);
        }
    }

    @Test
    void getClaims_ParseException_ThrowsCognitoTokenValidationException() {
        try (MockedStatic<SignedJWT> mockedStatic = mockStatic(SignedJWT.class)) {
            // Arrange
            mockedStatic.when(() -> SignedJWT.parse(VALID_TOKEN))
                    .thenThrow(new ParseException("Invalid token", 0));

            // Act & Assert
            assertThrows(CognitoTokenValidationException.class,
                    () -> CognitoTokenValidator.getClaims(VALID_TOKEN));
        }
    }
}