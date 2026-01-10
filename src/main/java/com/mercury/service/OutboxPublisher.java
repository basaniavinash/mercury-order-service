package com.mercury.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercury.model.*;
import com.mercury.repository.OutBoxEventsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxPublisher {

    private final OutBoxEventsRepository outboxRepo;
    private final OrderEventsProducer orderProducer;
    private final InventoryEventsProducer inventoryPublisher;
    private final ObjectMapper objectMapper;

    @Value("${outbox.max-attempts:10}")
    private int maxAttempts;

    public OutboxPublisher(
            OutBoxEventsRepository outBoxEventsRepository,
            OrderEventsProducer orderEventsProducer,
            InventoryEventsProducer inventoryEventsProducer,
            ObjectMapper objectMapper
    ) {
        this.outboxRepo = outBoxEventsRepository;
        this.orderProducer = orderEventsProducer;
        this.inventoryPublisher = inventoryEventsProducer;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.publish-interval-ms:1000}")
    public void publish() {
        List<OutboxEvent> batch = outboxRepo.claimPendingBatch(25);
        for (OutboxEvent row : batch) {
            publishOne(row);
        }
    }

    private void publishOne(OutboxEvent row) {
        try {
            // If your generated columns are working, this is already set.
            // If not, we can extract it from payload as a fallback.
            String eventType = row.getEventType();
            if (eventType == null || eventType.isBlank()) {
                eventType = extractEventType(row.getPayload());
            }

            switch (EventType.valueOf(eventType)) {

                case ORDER_CREATED -> {
                    OrderCreatedEvent evt = objectMapper.readValue(row.getPayload(), OrderCreatedEvent.class);
                    orderProducer.publishOrderEventCreated(row.getEventKey(), evt);
                }

                case INVENTORY_RESERVED -> {
                    InventoryReservedEvent evt = objectMapper.readValue(row.getPayload(), InventoryReservedEvent.class);
                    inventoryPublisher.publishInventoryReserved(row.getEventKey(), evt);
                }

                case INVENTORY_REJECTED -> {
                    InventoryRejectedEvent evt = objectMapper.readValue(row.getPayload(), InventoryRejectedEvent.class);
                    inventoryPublisher.publishInventoryRejected(row.getEventKey(), evt);
                }

                default -> throw new IllegalStateException("Unsupported event_type: " + eventType);
            }

            outboxRepo.markSent(row.getId(), Instant.now());
        } catch (Exception e) {
            int attempts = outboxRepo.incrementAttempt(row.getId(), Instant.now(), e.getMessage());

            if (attempts >= maxAttempts) {
                outboxRepo.markFailed(row.getId(), Instant.now(), e.getMessage());
            }
        }
    }

    private String extractEventType(String payload) throws Exception {
        JsonNode node = objectMapper.readTree(payload);
        JsonNode v = node.get("event_type");
        if (v == null || v.isNull()) throw new IllegalStateException("payload missing event_type");
        return v.asText();
    }
}