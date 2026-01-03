package com.mercury.repository;

import com.mercury.model.OrderItem;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class OrderItemsRepository {
    private final JdbcTemplate jdbcTemplate;

    public OrderItemsRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertOrderItems(List<OrderItem> items){
        String sql = """
                    INSERT INTO mercury.order_items
                    (order_id, item_id, qty, sku, unit_price, line_total)
                    VALUES
                    (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.batchUpdate(sql, items, 100, (ps, item) -> {
            ps.setLong(1, item.getOrderId().longValue());
            ps.setLong(2, item.getItemId().longValue());
            ps.setInt(3, item.getQty());
            ps.setString(4, item.getSku());
            ps.setBigDecimal(5, item.getUnitPrice());
            ps.setBigDecimal(6, item.getLineTotal());
        });
    }
}
