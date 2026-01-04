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

    //do it all in one query and fetch the failed items

    /*public class OutOfStockException extends RuntimeException {
        private final List<OutOfStockItem> items;

        public OutOfStockException(List<OutOfStockItem> items) {
            super("One or more items are out of stock");
            this.items = items;
        }

        public List<OutOfStockItem> getItems() {
            return items;
        }
    }

    public record OutOfStockItem(
            long itemId,
            int requestedQty,
            int availableQty
    ) {}

    public void reserveStock(List<OrderItem> items) {
        if (items.isEmpty()) return;

        // ---------- 1) Attempt atomic reservation ----------
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            if (i > 0) values.append(", ");
            values.append("(?, ?)");
            params.add(items.get(i).getItemId().longValue());
            params.add(items.get(i).getQty());
        }

        String reserveSql = """
        WITH req(item_id, qty) AS (
          VALUES %s
        ),
        updated AS (
          UPDATE mercury.items i
          SET available_qty = i.available_qty - r.qty,
              modified_at = now()
          FROM req r
          WHERE i.id = r.item_id
            AND i.available_qty >= r.qty
          RETURNING i.id
        )
        SELECT
          (SELECT count(*) FROM req)     AS requested,
          (SELECT count(*) FROM updated) AS updated
        """.formatted(values);

        Map<String, Object> row =
                jdbcTemplate.queryForMap(reserveSql, params.toArray());

        int requested = ((Number) row.get("requested")).intValue();
        int updated = ((Number) row.get("updated")).intValue();

        if (requested == updated) {
            return; // success
        }

        // ---------- 2) Identify failing items ----------
        String findFailuresSql = """
        SELECT
          r.item_id,
          r.qty AS requested_qty,
          i.available_qty
        FROM (
          VALUES %s
        ) AS r(item_id, qty)
        JOIN mercury.items i ON i.id = r.item_id
        WHERE i.available_qty < r.qty
        """.formatted(values);

        List<OutOfStockItem> failures =
                jdbcTemplate.query(findFailuresSql, params.toArray(),
                        (rs, rowNum) -> new OutOfStockItem(
                                rs.getLong("item_id"),
                                rs.getInt("requested_qty"),
                                rs.getInt("available_qty")
                        )
                );

        throw new OutOfStockException(failures);
    }*/
}
