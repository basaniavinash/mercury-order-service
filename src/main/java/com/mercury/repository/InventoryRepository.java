package com.mercury.repository;

import com.mercury.exception.OutOfStockException;
import com.mercury.model.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InventoryRepository {
    private final JdbcTemplate jdbcTemplate;

    InventoryRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void reserveStock(List<OrderItem> orderItems){
        String sql = """
                    UPDATE mercury.items SET available_qty = available_qty-? WHERE id = ? AND available_qty >= ?
                """;

        orderItems.forEach(orderItem -> {
            int updatedVal = jdbcTemplate.update(sql, ps -> {
                ps.setInt(1, orderItem.getQty());
                ps.setLong(2, orderItem.getItemId().longValue());
                ps.setInt(3, orderItem.getQty());
            });

            if(updatedVal == 0){
                throw new OutOfStockException(orderItem.getItemId(), orderItem.getQty());
            }
        });
    }
}
