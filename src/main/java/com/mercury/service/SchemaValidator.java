package com.mercury.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
public class SchemaValidator {

    private final ObjectMapper mapper;
    private final JsonSchemaFactory factory;

    private final JsonSchema orderCreatedSchema;
    private final JsonSchema inventoryReservedSchema;
    private final JsonSchema inventoryRejectedSchema;

    public SchemaValidator(
            ObjectMapper mapper,
            @Value("${mercury.schemas.orderCreated}") String orderCreatedPath,
            @Value("${mercury.schemas.inventoryReserved}") String inventoryReservedPath,
            @Value("${mercury.schemas.inventoryRejected}") String inventoryRejectedPath
    ) {
        this.mapper = mapper;
        this.factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

        this.orderCreatedSchema = load(orderCreatedPath);
        this.inventoryReservedSchema = load(inventoryReservedPath);
        this.inventoryRejectedSchema = load(inventoryRejectedPath);
    }

    private JsonSchema load(String path) {
        try (var is = Files.newInputStream(Path.of(path))) {
            return factory.getSchema(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load schema from path: " + path, e);
        }
    }

    public void validateOrderCreated(Object event) {
        validate(orderCreatedSchema, event, "OrderCreated");
    }

    public void validateInventoryReserved(Object event) {
        validate(inventoryReservedSchema, event, "InventoryReserved");
    }

    public void validateInventoryRejected(Object event) {
        validate(inventoryRejectedSchema, event, "InventoryRejected");
    }

    private void validate(JsonSchema schema, Object event, String name) {
        var node = mapper.valueToTree(event);
        var errors = schema.validate(node);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(name + " schema validation failed: " + errors);
        }
    }
}