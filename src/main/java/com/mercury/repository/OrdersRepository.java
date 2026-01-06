package com.mercury.repository;

import com.mercury.model.Order;
import com.mercury.model.OrderStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.sql.*;
import java.util.List;

@Repository
public class OrdersRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrdersRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        return Order.builder()
                .id(BigInteger.valueOf(rs.getLong("id")))
                .userId(BigInteger.valueOf(rs.getLong("user_id")))
                .status(OrderStatus.valueOf(rs.getString("status")))
                .subtotalAmount(rs.getBigDecimal("subtotal_amount"))
                .discountAmount(rs.getBigDecimal("discount_amount"))
                .taxAmount(rs.getBigDecimal("tax_amount"))
                .totalAmount(rs.getBigDecimal("total_amount"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .modifiedAt(rs.getTimestamp("modified_at").toInstant())
                .build();
    }
    public BigInteger createOrder(Order order){
        String sql = """
                    INSERT INTO mercury.orders
                    (user_id, status, subtotal_amount, discount_amount, tax_amount, total_amount)
                    VALUES
                    (?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (order.getUserId() == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, order.getUserId().longValue());
            }
            ps.setString(2, order.getStatus().name());
            ps.setBigDecimal(3, order.getSubtotalAmount());
            ps.setBigDecimal(4, order.getDiscountAmount());
            ps.setBigDecimal(5, order.getTaxAmount());
            ps.setBigDecimal(6, order.getTotalAmount());
            return ps;
        }, keyHolder);

        Number id = (Number) keyHolder.getKeys().get("id"); // because it might be a map
        return BigInteger.valueOf(id.longValue());
    }

    public void updateStatus(BigInteger orderId, OrderStatus status){
        String sql = "UPDATE mercury.orders SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status.name(), orderId.longValue());
    }

    public List<Order> getAllOrders(){
        String sql = """
                    select id, user_id, status, subtotal_amount, discount_amount, tax_amount, total_amount, created_at, modified_at
                    FROM mercury.orders;
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapOrder(rs));
    }

    public Order getOrderById(BigInteger id){
        String sql = """
                    select id, user_id, status, subtotal_amount, discount_amount, tax_amount, total_amount, created_at, modified_at
                    FROM mercury.orders WHERE id=?;
                """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapOrder(rs), id);
    }
}
