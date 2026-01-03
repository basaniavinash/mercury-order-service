package com.mercury.model;

import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private BigInteger id;

    private BigInteger userId;

    private OrderStatus status;

    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private Instant createdAt;
    private Instant modifiedAt;

    // not persisted directly — assembled at service layer
    private List<OrderItem> items;
}