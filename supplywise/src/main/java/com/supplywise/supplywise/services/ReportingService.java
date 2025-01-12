package com.supplywise.supplywise.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.supplywise.supplywise.model.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReportingService {
    private static final Logger logger = LoggerFactory.getLogger(ReportingService.class);
    private final HttpClient client;
    private final ObjectMapper objectMapper;
    
    private String apiGatewayUrl = "https://api-gateway-url.com"; //TODO mudar para o url do api gateway

    public ReportingService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public String generateReport(Inventory inventory) {
        try {
            // Create a JSON structure that matches the FastAPI expectations
            ObjectNode requestBody = objectMapper.createObjectNode();
            
            // Company data
            ObjectNode companyData = requestBody.putObject("company_data");
            companyData.put("id", inventory.getRestaurant().getCompany().getId().toString());
            companyData.put("name", inventory.getRestaurant().getCompany().getName());
            
            // Restaurant data
            ObjectNode restaurantData = requestBody.putObject("restaurant_data");
            restaurantData.put("id", inventory.getRestaurant().getId().toString());
            restaurantData.put("name", inventory.getRestaurant().getName());
            
            // User data
            ObjectNode userData = requestBody.putObject("user_data");
            userData.put("id", inventory.getClosedByUser());
            userData.put("name", inventory.getClosedByUser()); // Or fetch from user service if needed
            
            // Inventory data
            ObjectNode inventoryData = requestBody.putObject("inventory_data");
            inventoryData.put("starting_time", inventory.getEmissionDate().toString());
            inventoryData.put("closure_time", inventory.getClosingDate().toString());
            
            // Convert items
            inventoryData.putArray("items").addAll(
                inventory.getItems().stream()
                    .map(item -> {
                        ObjectNode itemNode = objectMapper.createObjectNode();
                        itemNode.put("id", item.getItem().getId().toString());
                        itemNode.put("name", item.getItem().getName());
                        itemNode.put("barcode", item.getItem().getBarCode());
                        itemNode.put("quantity", item.getQuantity());
                        itemNode.put("category", item.getItem().getCategory().name());
                        return itemNode;
                    })
                    .collect(Collectors.toList())
            );

            // Send request
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiGatewayUrl + "/reports/"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

            HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("Failed to generate report. Status: {}, Response: {}", 
                    response.statusCode(), response.body());
                return "Failed to generate report";
            }

            // Update inventory with report URL
            inventory.setReport(response.body());
            
            logger.info("Report generated successfully for inventory: {}", inventory.getId());
            return response.body();
        } catch (Exception e) {
            logger.error("Error generating report: ", e);
            return "Failed to generate report: " + e.getMessage();
        }
    }
}