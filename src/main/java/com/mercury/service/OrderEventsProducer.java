package com.mercury.service;

import com.mercury.model.EventType;
import com.mercury.model.Order;
import com.mercury.model.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderEventsProducer {

    private final SchemaValidator schemaValidator;
    @Value("${mercury.kafka.topics.order-created-topic}")
    private String orderCreatedTopic;

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    OrderEventsProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
                        SchemaValidator schemaValidator){
        this.kafkaTemplate = kafkaTemplate;
        this.schemaValidator = schemaValidator;
    }

    public void publishOrderEventCreated(String key, OrderCreatedEvent orderCreatedEvent){
        schemaValidator.validateOrderCreated(orderCreatedEvent);
        kafkaTemplate.send(orderCreatedTopic, key, orderCreatedEvent);
    }
}
