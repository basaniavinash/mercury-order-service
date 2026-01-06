package com.mercury.controller;

import com.mercury.model.Order;
import com.mercury.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService ;

    public OrderController(OrderService orderService){
        this.orderService = orderService;
    }

    @PostMapping
    public BigInteger createOrder(@RequestBody Order order){
        return orderService.createOrder(order);
    }

    @GetMapping
    public List<Order> getAllOrders(){
        return orderService.getAllOrders();
    }

    @GetMapping(value = "/{id}")
    public Order getOrderById(@PathVariable BigInteger id){
        return orderService.getOrderById(id);
    }
}
