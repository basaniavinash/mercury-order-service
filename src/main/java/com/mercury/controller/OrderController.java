package com.mercury.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    public BigInteger createOrder(@RequestBody Order order) throws JsonProcessingException {
        return orderService.createOrder(order);
    }

    @PostMapping(value = "/timeout")
    public BigInteger createOrderTimeout(@RequestBody Order order){
        return orderService.createOrderTimeout(order);
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
