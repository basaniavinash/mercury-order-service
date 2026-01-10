package com.mercury.service;

import com.mercury.model.InventoryRejectedEvent;
import com.mercury.model.InventoryReservedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventsProducer {

    private final KafkaTemplate<String, InventoryReservedEvent> reservedKafka;
    private final KafkaTemplate<String, InventoryRejectedEvent> rejectedKafka;
    private final SchemaValidator schemaValidator;

    @Value("${mercury.kafka.topics.inventory-reserved-topic}")
    private String inventoryReservedTopic;

    @Value("${mercury.kafka.topics.inventory-rejected-topic}")
    private String inventoryRejectedTopic;

    public InventoryEventsProducer(
            KafkaTemplate<String, InventoryReservedEvent> reservedKafka,
            KafkaTemplate<String, InventoryRejectedEvent> rejectedKafka,
            SchemaValidator schemaValidator
    ) {
        this.reservedKafka = reservedKafka;
        this.rejectedKafka = rejectedKafka;
        this.schemaValidator = schemaValidator;
    }

    public void publishInventoryReserved(String key, InventoryReservedEvent evt) {
        schemaValidator.validateInventoryReserved(evt);
        reservedKafka.send(inventoryReservedTopic, key, evt);
    }

    public void publishInventoryRejected(String key, InventoryRejectedEvent evt) {
        schemaValidator.validateInventoryRejected(evt);
        rejectedKafka.send(inventoryRejectedTopic, key, evt);
    }
}