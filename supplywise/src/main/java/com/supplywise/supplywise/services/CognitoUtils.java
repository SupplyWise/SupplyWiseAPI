package com.supplywise.supplywise.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.stereotype.Component;

import com.supplywise.supplywise.model.Company;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;


@Component
public class CognitoUtils {

    private final Logger logger = LoggerFactory.getLogger(CognitoUtils.class);

    private final AuthHandler authHandler;

    @Autowired
    public CognitoUtils(AuthHandler authHandler) {
        this.authHandler = authHandler;
    }
    
    public String promoteDisassociatedToOwner(Company company) {
        try {
            String url = "https://zo9bnne4ec.execute-api.eu-west-1.amazonaws.com/dev/user-management/promote_to_owner";
            HttpClient client = HttpClient.newHttpClient();
            String requestBody = String.format("{\"company_id\": \"%s\"}", company.getId().toString());
            HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + authHandler.getAuthenticatedAccessToken())
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Failed to promote user to FRANCHISE_OWNER");
                // Details about the error can be found in the request response
                return response.body();
            }

            logger.info("User promoted to FRANCHISE_OWNER successfully");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread was interrupted while promoting user to FRANCHISE_OWNER", e);
            return "Failed to promote user to FRANCHISE_OWNER.";
        } catch (Exception e) {
            logger.error("Exception occurred while promoting user to FRANCHISE_OWNER", e);
            return "Failed to promote user to FRANCHISE_OWNER.";
        }

        return null;
    }
}
