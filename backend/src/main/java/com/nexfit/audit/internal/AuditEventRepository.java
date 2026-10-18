package com.nexfit.audit.internal;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class AuditEventRepository {

    private final JdbcTemplate jdbc;
    private final JsonMapper json;

    AuditEventRepository(JdbcTemplate jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    public void insert(String eventType, Long actorUserId, Long organizationId,
                       String entityType, Long entityId, String ip, Map<String, Object> metadata) {
        jdbc.update("""
            INSERT INTO audit_events
                (organization_id, actor_user_id, event_type, entity_type, entity_id, ip_address, metadata)
            VALUES (?, ?, ?, ?, ?, CAST(? AS inet), CAST(? AS jsonb))
            """,
            organizationId, actorUserId, eventType, entityType, entityId, ip,
            json.writeValueAsString(metadata));
    }
}
