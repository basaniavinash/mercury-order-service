package com.mercury.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercury.model.*;
import com.mercury.repository.InventoryRepository;
import com.mercury.repository.OrderItemsRepository;
import com.mercury.repository.OrdersRepository;
import com.mercury.repository.OutBoxEventsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    @Value("${mercury.kafka.topics.order-created-topic}")
    private String orderCreatedTopic;

    @Value("${mercury.kafka.topics.inventory-reserved-topic}")
    private String inventoryReservedTopic;

    @Value("${mercury.kafka.topics.inventory-rejected-topic}")
    private String inventoryRejectedTopic;
    private final OrderItemsRepository orderItemsRepository;
    private final OrdersRepository ordersRepository;
    private final InventoryRepository inventoryRepository;
    private final OutBoxEventsRepository outBoxEventsRepository;
    private final ObjectMapper objectMapper;
    public OrderService(OrdersRepository ordersRepository,
                        OrderItemsRepository orderItemsRepository,
                        InventoryRepository inventoryRepository,
                        OutBoxEventsRepository outBoxEventsRepository,
                        ObjectMapper objectMapper){
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.inventoryRepository = inventoryRepository;
        this.outBoxEventsRepository = outBoxEventsRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(timeout = 5)
    public BigInteger createOrder(Order order) throws JsonProcessingException {
        // 1) start as NEW
        order.setStatus(OrderStatus.NEW);

        // 2) create order row -> get id
        BigInteger orderId = ordersRepository.createOrder(order);
        order.setId(orderId);

        // 3) set orderId on items
        order.getItems().forEach(i -> i.setOrderId(orderId));

        // 4) reserve stock (throws if not enough)
        inventoryRepository.reserveStock(order.getItems());

        /*outBoxEventsRepository.createOutboxEvent(buildInventoryReservedOutboxEvent(order));*/

        // 5) insert order_items
        orderItemsRepository.insertOrderItems(order.getItems());

        // 6) mark CREATED
        ordersRepository.updateStatus(orderId, OrderStatus.CREATED);

        outBoxEventsRepository.createOutboxEvent(buildOutboxEvent(order));

        return orderId;
    }

    private OutboxEvent buildOutboxEvent(Order order) throws JsonProcessingException {
        OrderCreatedEvent orderCreatedEvent = OrderCreatedEvent.builder()
                .orderId(String.valueOf(order.getId()))
                .eventId(String.valueOf(UUID.randomUUID()))
                .correlationId(String.valueOf(UUID.randomUUID()))
                .eventType(EventType.ORDER_CREATED.name())
                .producedAt(Instant.now())
                .schemaVersion("v1")
                .currency("USD")
                .items(order.getItems().stream()
                        .map(i -> new OrderCreatedEventItem(i.getSku(), i.getQty(), i.getUnitPrice()))
                        .toList())
                .totalAmount(order.getTotalAmount().doubleValue())
                .userId(String.valueOf(order.getUserId()))
                .build();

        return OutboxEvent.builder()
                .topic(orderCreatedTopic)
                .eventKey(String.valueOf(order.getId()))
                .payload(objectMapper.writeValueAsString(orderCreatedEvent))
                .status(OutBoxStatus.PENDING.name())
                .build();
    }

    /*private OutboxEvent buildInventoryReservedOutboxEvent(Order order) throws JsonProcessingException {
        var evt = InventoryReservedEvent.builder()
                .orderId(String.valueOf(order.getId()))
                .eventId(String.valueOf(UUID.randomUUID()))
                .reservationId(String.valueOf(UUID.randomUUID()))
                .correlationId(String.valueOf(UUID.randomUUID()))
                .eventType(EventType.INVENTORY_RESERVED.name())
                .producedAt(Instant.now())
                .schemaVersion("v1")
                .items(order.getItems().stream()
                        .map(i -> InventoryReservedEvent.Item.builder()
                                .sku(i.getSku())
                                .qty(i.getQty())
                                .build())
                        .toList()
                )
                .build();

        return OutboxEvent.builder()
                .topic(inventoryReservedTopic)
                .eventKey(String.valueOf(order.getId()))
                .payload(objectMapper.writeValueAsString(evt))
                .status(OutBoxStatus.PENDING.name())
                .build();
    }*/

    @Transactional(timeout = 5)
    public BigInteger createOrderTimeout(Order order){
        // 1) start as NEW
        order.setStatus(OrderStatus.NEW);

        // 2) create order row -> get id
        BigInteger orderId = ordersRepository.createOrder(order);
        order.setId(orderId);

        // 3) set orderId on items
        order.getItems().forEach(i -> i.setOrderId(orderId));

        // 4) reserve stock (throws if not enough)
        inventoryRepository.reserveStock(order.getItems());

        // 5) insert order_items
        orderItemsRepository.insertOrderItems(order.getItems());

        // 6) mark CREATED
        ordersRepository.updateStatusTimeout(orderId, OrderStatus.CREATED);

        return orderId;
    }

    public List<Order> getAllOrders(){
        return ordersRepository.getAllOrders();
    }

    public Order getOrderById(BigInteger id){
        return ordersRepository.getOrderById(id);
    }
}
