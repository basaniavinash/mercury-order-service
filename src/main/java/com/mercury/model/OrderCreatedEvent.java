package com.mercury.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public record OrderCreatedEvent(

        String eventId,
        String correlationId,
        Instant producedAt,
        String eventType,
        String schemaVersion,

        String orderId,
        String userId,
        double totalAmount,
        String currency,
        List<OrderCreatedEventItem> items
) {
    public record Item(
            String sku,
            int qty,
            double unitPrice
    ) {}
}
