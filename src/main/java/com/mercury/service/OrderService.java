package com.mercury.service;

import com.mercury.model.Order;
import com.mercury.model.OrderStatus;
import com.mercury.repository.InventoryRepository;
import com.mercury.repository.OrderItemsRepository;
import com.mercury.repository.OrdersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Service
public class OrderService {
    private final OrderItemsRepository orderItemsRepository;
    private final OrdersRepository ordersRepository;
    private final InventoryRepository inventoryRepository;
    public OrderService(OrdersRepository ordersRepository,
                        OrderItemsRepository orderItemsRepository,
                        InventoryRepository inventoryRepository){
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public BigInteger createOrder(Order order){
        // 1) start as NEW
        order.setStatus(OrderStatus.NEW);

        // 2) create order row -> get id
        BigInteger orderId = ordersRepository.createOrder(order);

        // 3) set orderId on items
        order.getItems().forEach(i -> i.setOrderId(orderId));

        // 4) reserve stock (throws if not enough)
        inventoryRepository.reserveStock(order.getItems());

        // 5) insert order_items
        orderItemsRepository.insertOrderItems(order.getItems());

        // 6) mark CREATED
        ordersRepository.updateStatus(orderId, OrderStatus.CREATED);

        return orderId;
    }

    public List<Order> getAllOrders(){
        return ordersRepository.getAllOrders();
    }
}
