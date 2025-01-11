package com.supplywise.supplywise.DAO;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddItemToInventoryRequest {
    private int barCode;
    private int quantity;
    private LocalDate expirationDate;
}
