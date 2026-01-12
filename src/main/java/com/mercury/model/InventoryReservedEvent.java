package com.mercury.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public record InventoryReservedEvent(

        String eventId,
        String correlationId,
        Instant producedAt,
        String eventType,
        String schemaVersion,

        String reservationId,

        String orderId,
        List<Item> items
) {
    @Builder
    public record Item(
            String sku,
            int qty
    ) {}
}