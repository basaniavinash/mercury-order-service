package com.mercury.repository;

import com.mercury.model.OutboxEvent;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class OutBoxEventsRepository {
    private final JdbcTemplate jdbcTemplate;

    OutBoxEventsRepository(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createOutboxEvent(OutboxEvent outboxEvent) {
        String sql = """
        INSERT INTO mercury.outbox_events(topic, event_key, payload, status)
        VALUES (?, ?, ?, ?)
        """;

        jdbcTemplate.update(conn -> {
            var ps = conn.prepareStatement(sql);

            ps.setString(1, outboxEvent.getTopic());
            ps.setString(2, outboxEvent.getEventKey());

            PGobject jsonb = new PGobject();
            jsonb.setType("jsonb");
            jsonb.setValue(outboxEvent.getPayload()); // must be valid JSON string
            ps.setObject(3, jsonb);

            ps.setString(4, outboxEvent.getStatus());
            return ps;
        });
    }

    public List<OutboxEvent> claimPendingBatch(int limit) {
        String sql = """
        SELECT id, topic, event_key, payload, attempts, last_attempt_at
        FROM mercury.outbox_events
        WHERE status = 'PENDING'
        ORDER BY id
        FOR UPDATE SKIP LOCKED
        LIMIT ?
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> OutboxEvent.builder()
                .id(rs.getLong("id"))
                .topic(rs.getString("topic"))
                .eventKey(rs.getString("event_key"))
                .payload(rs.getString("payload"))
                .attempts(rs.getInt("attempts"))
                .lastAttemptAt(rs.getTimestamp("last_attempt_at") == null ? null
                        : rs.getTimestamp("last_attempt_at").toInstant())
                .status("PENDING") // optional, but fine
                .build(), limit);
    }

    public void markSent(long id, Instant sentAt) {
        String sql = """
            UPDATE mercury.outbox_events
            SET status = 'SENT',
                sent_at = ?,
                modified_at = now(),
                last_error = NULL
            WHERE id = ?
            """;
        jdbcTemplate.update(sql, Timestamp.from(sentAt), id);
    }

    public int incrementAttempt(long id, Instant when, String err) {
        String sql = """
        UPDATE mercury.outbox_events
        SET attempts = attempts + 1,
            last_attempt_at = ?,
            last_error = ?,
            modified_at = now()
        WHERE id = ?
        RETURNING attempts
        """;
        Integer attempts = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                Timestamp.from(when),
                err,
                id
        );
        return attempts == null ? 0 : attempts;
    }

    public void markFailed(long id, Instant when, String err) {
        String sql = """
        UPDATE mercury.outbox_events
        SET status = 'FAILED',
            last_attempt_at = ?,
            last_error = ?,
            modified_at = now()
        WHERE id = ?
        """;
        jdbcTemplate.update(sql, Timestamp.from(when), err, id);
    }


}
