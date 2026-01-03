package com.mercury.model;

import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private BigInteger id;

    private BigInteger orderId;
    private BigInteger itemId;

    private int qty;

    private String sku;

    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    private Instant createdAt;
    private Instant modifiedAt;
}