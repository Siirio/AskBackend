CREATE TABLE significant_event (
    id UUID NOT NULL PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    actor_user_id UUID,
    event_type VARCHAR(64) NOT NULL,
    business_id UUID,
    entity_id UUID,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb
);

CREATE INDEX idx_significant_event_business_created
    ON significant_event (business_id, created_at);

CREATE INDEX idx_significant_event_type_created
    ON significant_event (event_type, created_at);

ALTER TABLE product
    ADD COLUMN hidden_by_moderator BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE chat_attachment (
    id UUID NOT NULL PRIMARY KEY,
    conversation_id UUID NOT NULL,
    stored_name VARCHAR(128) NOT NULL UNIQUE,
    original_name VARCHAR(512) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    uploaded_by_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_chat_attachment_conversation
    ON chat_attachment (conversation_id);

DROP TABLE IF EXISTS branch_invite;
