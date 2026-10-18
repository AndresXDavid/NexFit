-- NEX-14 (TT-02) · Auditoría de solo inserción (RN-40)

CREATE FUNCTION fn_forbid_update() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'La tabla % es de solo inserción: UPDATE no permitido (RN-40)', TG_TABLE_NAME;
END;
$$;

CREATE FUNCTION fn_forbid_delete() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'La tabla % es de solo inserción: DELETE/TRUNCATE no permitido (RN-40)', TG_TABLE_NAME;
END;
$$;

CREATE TRIGGER trg_audit_events_no_update
    BEFORE UPDATE ON audit_events
    FOR EACH ROW EXECUTE FUNCTION fn_forbid_update();

CREATE TRIGGER trg_audit_events_no_delete
    BEFORE DELETE ON audit_events
    FOR EACH ROW EXECUTE FUNCTION fn_forbid_delete();

CREATE TRIGGER trg_audit_events_no_truncate
    BEFORE TRUNCATE ON audit_events
    FOR EACH STATEMENT EXECUTE FUNCTION fn_forbid_delete();

CREATE INDEX idx_audit_events_occurred_at ON audit_events (occurred_at);
CREATE INDEX idx_audit_events_actor_user  ON audit_events (actor_user_id);
CREATE INDEX idx_audit_events_event_type  ON audit_events (event_type);
