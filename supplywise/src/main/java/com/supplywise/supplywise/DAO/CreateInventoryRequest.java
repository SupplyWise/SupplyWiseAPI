package com.supplywise.supplywise.DAO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequest {
    private UUID restaurantId;
    private LocalDateTime emissionDate;
    private LocalDateTime expectedClosingDate;  // optional, can be null
}
