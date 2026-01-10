package com.mercury.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public record InventoryRejectedEvent(

        String eventId,
        String correlationId,
        Instant producedAt,
        String eventType,
        String schemaVersion,

        String orderId,
        String reason,
        List<Item> items
) {
    @Builder
    public record Item(
            String sku,
            int qty
    ) {}
}