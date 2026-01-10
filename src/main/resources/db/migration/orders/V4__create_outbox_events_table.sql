CREATE TABLE IF NOT EXISTS mercury.outbox_events (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

  topic      TEXT NOT NULL,
  event_key  TEXT NOT NULL,
  payload    JSONB NOT NULL,

  event_id       UUID GENERATED ALWAYS AS ((payload->>'event_id')::uuid) STORED,
  correlation_id UUID GENERATED ALWAYS AS ((payload->>'correlation_id')::uuid) STORED,
  event_type     TEXT GENERATED ALWAYS AS (payload->>'event_type') STORED,
  schema_version TEXT GENERATED ALWAYS AS (payload->>'schema_version') STORED,

  status TEXT NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING','SENT','FAILED')),

  sent_at TIMESTAMPTZ,
  attempts INT NOT NULL DEFAULT 0,
  last_attempt_at TIMESTAMPTZ,
  last_error TEXT,

  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  modified_at TIMESTAMPTZ NOT NULL DEFAULT now()
);