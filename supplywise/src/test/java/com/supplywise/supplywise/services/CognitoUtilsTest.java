package com.supplywise.supplywise.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.supplywise.supplywise.model.Company;

import org.slf4j.Logger;

class CognitoUtilsTest {

    @Mock
    private AuthHandler authHandler;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse;

    @Mock
    private Logger logger;

    private CognitoUtils cognitoUtils;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cognitoUtils = new CognitoUtils(authHandler, httpClient);
    }

    @Test
    void promoteDisassociatedToOwner_SuccessfulPromotion_ReturnsBody() throws Exception {
        // Arrange
        Company mockCompany = new Company();
        mockCompany.setId(UUID.randomUUID());

        when(authHandler.getAuthenticatedAccessToken()).thenReturn("mock.token.value");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn((HttpResponse<String>) httpResponse);
        when(httpResponse.statusCode()).thenReturn(200);
        when(httpResponse.body()).thenReturn("Success");

        // Act
        String result = cognitoUtils.promoteDisassociatedToOwner(mockCompany);

        // Assert
        assertNull(result);
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void promoteDisassociatedToOwner_FailedPromotion_ReturnsErrorMessage() throws Exception {
        // Arrange
        Company mockCompany = new Company();
        mockCompany.setId(UUID.randomUUID());

        when(authHandler.getAuthenticatedAccessToken()).thenReturn("mock-token");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(httpResponse);
        when(httpResponse.statusCode()).thenReturn(400);
        when(httpResponse.body()).thenReturn("Failed to promote");

        // Act
        String result = cognitoUtils.promoteDisassociatedToOwner(mockCompany);

        // Assert
        assertEquals("Failed to promote", result);
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void promoteDisassociatedToOwner_InterruptedException_ReturnsErrorMessage() throws Exception {
        // Arrange
        Company mockCompany = new Company();
        mockCompany.setId(UUID.randomUUID());

        when(authHandler.getAuthenticatedAccessToken()).thenReturn("mock-token");
        doThrow(new InterruptedException()).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        // Act
        String result = cognitoUtils.promoteDisassociatedToOwner(mockCompany);

        // Assert
        assertEquals("Failed to promote user to FRANCHISE_OWNER.", result);
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void promoteDisassociatedToOwner_GenericException_ReturnsErrorMessage() throws Exception {
        // Arrange
        Company mockCompany = new Company();
        mockCompany.setId(UUID.randomUUID());

        when(authHandler.getAuthenticatedAccessToken()).thenReturn("mock-token");
        doThrow(new RuntimeException("Unexpected error")).when(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

        // Act
        String result = cognitoUtils.promoteDisassociatedToOwner(mockCompany);

        // Assert
        assertEquals("Failed to promote user to FRANCHISE_OWNER.", result);
        verify(httpClient).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
}