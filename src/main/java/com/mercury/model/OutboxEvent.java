package com.mercury.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    // ---------- DB GENERATED ----------

    private Long id;

    private UUID eventId;
    private UUID correlationId;
    private String eventType;
    private String schemaVersion;

    private Instant createdAt;
    private Instant modifiedAt;

    // ---------- APP OWNED ----------

    private String topic;
    private String eventKey;

    /**
     * Raw JSON string stored in JSONB column.
     * This is your source of truth.
     */
    private String payload;

    /**
     * PENDING | SENT | FAILED
     */
    private String status;

    private int attempts;

    private Instant sentAt;
    private Instant lastAttemptAt;

    private String lastError;
}