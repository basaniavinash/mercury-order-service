package com.mercury.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record OrderCreatedEventItem(
        String sku,
        int qty,
        BigDecimal unitPrice
) {}